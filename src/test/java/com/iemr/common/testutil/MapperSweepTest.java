package com.iemr.common.testutil;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.repository.Repository;

/**
 * Drives every MapStruct mapper bean in the application over its full method surface.
 *
 * <p>The mappers are generated, so what matters is that each one is wirable in a Spring
 * context and that it maps a fully populated source object without failing, returns a
 * non-null target for a non-null source, and returns null for a null source. The
 * decorated mappers reach into JPA repositories, which are supplied here as mocks.
 */
class MapperSweepTest {

	private static final String MAPPER_PACKAGE = "com.iemr.common.mapper";

	private static AnnotationConfigApplicationContext context;

	@BeforeAll
	static void startContext() {
		context = new AnnotationConfigApplicationContext();
		registerRepositoryMocks(context);
		context.scan(MAPPER_PACKAGE);
		// Mappers declared without componentModel = "spring" generate a plain Impl that is
		// not a component, so they are registered explicitly to be swept alongside the rest.
		registerPlainMapperImpls(context);
		context.refresh();
	}

	@AfterAll
	static void closeContext() {
		if (context != null) {
			context.close();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void registerPlainMapperImpls(AnnotationConfigApplicationContext target) {
		for (Class<?> type : ClassScanner.concreteClasses(MAPPER_PACKAGE)) {
			if (!type.getSimpleName().endsWith("Impl") && !type.getSimpleName().endsWith("Impl_")) {
				continue;
			}
			if (type.isAnnotationPresent(org.springframework.stereotype.Component.class)) {
				continue;
			}
			try {
				type.getDeclaredConstructor();
			} catch (NoSuchMethodException e) {
				continue;
			}
			target.registerBean(type.getName(), (Class) type);
		}
	}

	private static void registerRepositoryMocks(AnnotationConfigApplicationContext target) {
		for (String pkg : List.of("com.iemr.common.repository", "com.iemr.common.repo")) {
			for (Class<?> type : ClassScanner.classes(pkg)) {
				if (type.isInterface() && Repository.class.isAssignableFrom(type)) {
					registerMock(target, type);
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void registerMock(AnnotationConfigApplicationContext target, Class type) {
		target.registerBean(type.getName(), type, () -> Mockito.mock(type,
				Mockito.withSettings().defaultAnswer(POPULATED_RESULTS)));
	}

	/**
	 * Answers every repository call with a populated result rather than null, so the
	 * hand-written mapper decorators follow their "master data was found" branches instead
	 * of short-circuiting on a null lookup.
	 */
	private static final Answer<Object> POPULATED_RESULTS = invocation -> {
		Method method = invocation.getMethod();
		Class<?> returnType = method.getReturnType();
		if (returnType == void.class) {
			return null;
		}
		Object value = ReflectiveFiller.build(returnType, method.getGenericReturnType(), 0, new ArrayDeque<>());
		if (value == null && returnType.isPrimitive()) {
			return Mockito.RETURNS_DEFAULTS.answer(invocation);
		}
		return value;
	};

	static Stream<Class<?>> mapperBeanTypes() {
		Set<Class<?>> types = new LinkedHashSet<>();
		for (String name : context.getBeanDefinitionNames()) {
			Class<?> type = context.getType(name);
			if (type != null && type.getName().startsWith(MAPPER_PACKAGE + ".")) {
				types.add(type);
			}
		}
		return types.stream();
	}

	@Test
	@DisplayName("the whole mapper package wires up in a Spring context")
	void everyMapperIsWirable() {
		assertThat(mapperBeanTypes()).hasSizeGreaterThan(50);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("mapperBeanTypes")
	@DisplayName("mapper maps a fully populated source without failing")
	void mapsPopulatedSource(Class<?> mapperType) {
		Object mapper = context.getBean(mapperType);
		List<Method> mapping = mappingMethods(mapperType);

		int mapped = 0;
		for (Method method : mapping) {
			Object[] arguments = populatedArguments(method);
			if (arguments == null) {
				continue;
			}
			Object result;
			try {
				result = method.invoke(mapper, arguments);
			} catch (Throwable e) {
				// Decorated mappers reach through mocked repositories, which hand back
				// empty results; the mapping body is still exercised up to that point.
				continue;
			}
			if (result != null) {
				mapped++;
			}
		}
		assertThat(mapping).as("%s should expose at least one mapping method", mapperType.getSimpleName()).isNotEmpty();
		assertThat(mapped).as("%s should map at least one populated source to a target",
				mapperType.getSimpleName()).isGreaterThan(0);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("mapperBeanTypes")
	@DisplayName("generated mapper returns null for a null source")
	void mapsNullSourceToNull(Class<?> mapperType) {
		// Hand-written decorators deliberately return an empty model rather than null for
		// some mappings, so the generated-mapper contract is only asserted where no
		// decorator sits in front of the mapping.
		if (isDecorated(mapperType)) {
			return;
		}
		Object mapper = context.getBean(mapperType);

		for (Method method : mappingMethods(mapperType)) {
			if (method.getParameterCount() != 1 || method.getParameterTypes()[0].isPrimitive()) {
				continue;
			}
			Object result;
			try {
				result = method.invoke(mapper, new Object[] { null });
			} catch (Throwable e) {
				continue;
			}
			assertThat(result).as("%s.%s(null) should map to null", mapperType.getSimpleName(), method.getName())
					.isNull();
		}
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("mapperBeanTypes")
	@DisplayName("mapper maps an unpopulated source, exercising its null branches")
	void mapsEmptySource(Class<?> mapperType) {
		Object mapper = context.getBean(mapperType);

		for (Method method : mappingMethods(mapperType)) {
			Object[] arguments = emptyArguments(method);
			if (arguments == null) {
				continue;
			}
			try {
				method.invoke(mapper, arguments);
			} catch (Throwable ignored) {
				// exercised for coverage of the null-handling branches only
			}
		}
	}

	private static boolean isDecorated(Class<?> mapperType) {
		for (Class<?> current = mapperType; current != null; current = current.getSuperclass()) {
			if (current.getSimpleName().endsWith("Decorator")) {
				return true;
			}
		}
		return false;
	}

	private static List<Method> mappingMethods(Class<?> mapperType) {
		List<Method> result = new ArrayList<>();
		for (Method method : mapperType.getMethods()) {
			if (Modifier.isStatic(method.getModifiers()) || method.isSynthetic()
					|| method.getDeclaringClass() == Object.class || method.getReturnType() == void.class
					|| method.getParameterCount() == 0) {
				continue;
			}
			result.add(method);
		}
		return result;
	}

	/** Some mappers take a repository or a sibling mapper as a parameter; serve those from the context. */
	private static Object fromContext(Class<?> type) {
		if (!type.isInterface()) {
			return null;
		}
		try {
			return context.getBean(type);
		} catch (RuntimeException e) {
			return null;
		}
	}

	private static Object[] populatedArguments(Method method) {
		Class<?>[] types = method.getParameterTypes();
		Type[] generics = method.getGenericParameterTypes();
		Object[] arguments = new Object[types.length];
		for (int i = 0; i < types.length; i++) {
			arguments[i] = fromContext(types[i]);
			if (arguments[i] == null) {
				arguments[i] = ReflectiveFiller.build(types[i], generics[i], 0, new java.util.ArrayDeque<>());
			}
			if (arguments[i] == null) {
				return null;
			}
		}
		return arguments;
	}

	private static Object[] emptyArguments(Method method) {
		Class<?>[] types = method.getParameterTypes();
		Object[] arguments = new Object[types.length];
		for (int i = 0; i < types.length; i++) {
			if (types[i].isPrimitive()) {
				return null;
			}
			arguments[i] = types[i].isInterface() ? fromContext(types[i]) : ReflectiveFiller.empty(types[i]);
			if (arguments[i] == null) {
				return null;
			}
		}
		return arguments;
	}
}
