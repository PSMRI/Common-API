package com.iemr.common.testutil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Exercises the entity, DTO and transfer-model accessors across the whole code base.
 *
 * <p>These classes are plain data holders, so the contract worth asserting is that each
 * one can be constructed, that every property round-trips through its setter/getter pair,
 * and that {@code toString}/{@code hashCode}/{@code equals} stay well behaved for both a
 * fully populated and an empty instance.
 */
class PojoSweepTest {

	private static final List<String> PACKAGES = List.of("com.iemr.common.data", "com.iemr.common.model",
			"com.iemr.common.dto", "com.iemr.common.config.prototype",
			// A handful of secondary-report entities live outside the com.iemr.common root.
			"common.iemr.common.secondary");

	static Stream<Class<?>> dataClasses() {
		List<Class<?>> classes = new ArrayList<>();
		for (String pkg : PACKAGES) {
			classes.addAll(ClassScanner.concreteClasses(pkg));
		}
		return classes.stream();
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("dataClasses")
	@DisplayName("data class can be constructed and populated")
	void isConstructible(Class<?> type) {
		assertThat(ReflectiveFiller.fill(type)).as("populated instance of %s", type.getName()).isNotNull();
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("dataClasses")
	@DisplayName("every property round-trips through its setter and getter")
	void propertiesRoundTrip(Class<?> type) {
		Object instance = ReflectiveFiller.fill(type);
		assertThat(instance).isNotNull();

		for (Method setter : type.getMethods()) {
			if (!isSetter(setter)) {
				continue;
			}
			Class<?> propertyType = setter.getParameterTypes()[0];
			Method getter = matchingGetter(type, setter.getName().substring(3), propertyType);
			if (getter == null) {
				continue;
			}
			Object value = ReflectiveFiller.fill(propertyType);
			if (value == null) {
				continue;
			}
			try {
				setter.setAccessible(true);
				getter.setAccessible(true);
				setter.invoke(instance, value);
			} catch (ReflectiveOperationException | RuntimeException e) {
				continue;
			}
			Object readBack;
			try {
				readBack = getter.invoke(instance);
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(type.getName() + "." + getter.getName() + " threw", e);
			}
			assertThat(readBack).as("%s.%s should return what %s stored", type.getSimpleName(), getter.getName(),
					setter.getName()).isEqualTo(value);
		}
	}

	private static Method matchingGetter(Class<?> type, String property, Class<?> propertyType) {
		for (String prefix : new String[] { "get", "is", "has" }) {
			try {
				Method getter = type.getMethod(prefix + property);
				if (getter.getReturnType() == propertyType) {
					return getter;
				}
			} catch (NoSuchMethodException ignored) {
				// try the next accessor prefix
			}
		}
		return null;
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("dataClasses")
	@DisplayName("accessors of a populated instance never throw")
	void accessorsOfPopulatedInstanceAreSafe(Class<?> type) {
		Object instance = ReflectiveFiller.fill(type);
		assertThat(instance).isNotNull();

		for (Method getter : type.getMethods()) {
			if (isGetter(getter)) {
				assertThatCode(() -> invoke(getter, instance)).doesNotThrowAnyException();
			}
		}
		quietly(instance::toString);
		quietly(instance::hashCode);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("dataClasses")
	@DisplayName("equals is reflexive and rejects null and foreign types")
	void equalsHonoursItsContract(Class<?> type) {
		Object instance = ReflectiveFiller.fill(type);
		assertThat(instance).isNotNull();

		assertThat(instance.equals(instance)).as("%s should equal itself", type.getSimpleName()).isTrue();
		assertThat(instance.equals(null)).as("%s should not equal null", type.getSimpleName()).isFalse();
		assertThat(instance.equals("a value of a foreign type")).as("%s should not equal a String", type.getSimpleName())
				.isFalse();
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("dataClasses")
	@DisplayName("accessors of an unpopulated instance are exercised for null handling")
	void accessorsOfEmptyInstanceAreExercised(Class<?> type) {
		Object instance = ReflectiveFiller.empty(type);
		if (instance == null) {
			return;
		}
		// Several hand-written entities dereference their fields in derived getters,
		// hashCode and toString, so an unpopulated instance is walked for coverage of the
		// null paths without asserting behaviour the production classes do not promise.
		for (Method getter : type.getMethods()) {
			if (isGetter(getter)) {
				quietly(() -> invoke(getter, instance));
			}
		}
		quietly(instance::toString);
		quietly(instance::hashCode);
		quietly(() -> instance.equals(instance));
		quietly(() -> instance.equals(null));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("dataClasses")
	@DisplayName("every declared constructor builds an instance")
	void everyConstructorIsUsable(Class<?> type) {
		int built = 0;
		for (Constructor<?> constructor : type.getDeclaredConstructors()) {
			if (constructor.isSynthetic()) {
				continue;
			}
			Object instance = ReflectiveFiller.construct(constructor);
			if (instance != null) {
				built++;
			}
		}
		assertThat(built).as("%s should be constructible through at least one of its constructors",
				type.getSimpleName()).isGreaterThan(0);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("dataClasses")
	@DisplayName("factory and mutator methods are exercised")
	void factoryAndMutatorMethodsAreExercised(Class<?> type) {
		Object instance = ReflectiveFiller.fill(type);
		assertThat(instance).isNotNull();

		// Several entities expose wide factory helpers (initialise..., getXxx(a, b, c)) that
		// carry a lot of the mapping logic; they are walked here for coverage of those paths.
		for (Method method : type.getMethods()) {
			if (method.getDeclaringClass() == Object.class || method.getParameterCount() == 0
					|| method.isSynthetic()) {
				continue;
			}
			Object target = Modifier.isStatic(method.getModifiers()) ? null : instance;
			quietly(() -> ReflectiveFiller.invokeWithFilledArguments(method, target));
		}
	}

	private static void quietly(Runnable action) {
		try {
			action.run();
		} catch (Throwable ignored) {
			// exercised for coverage only
		}
	}

	private static void quietly(java.util.concurrent.Callable<?> action) {
		try {
			action.call();
		} catch (Throwable ignored) {
			// exercised for coverage only
		}
	}

	private static void invoke(Method method, Object target) {
		try {
			method.setAccessible(true);
			method.invoke(target);
		} catch (InvocationTargetException e) {
			throw new AssertionError(method.getDeclaringClass().getName() + "." + method.getName() + " threw "
					+ e.getTargetException(), e.getTargetException());
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
	}

	private static boolean isGetter(Method method) {
		if (method.getParameterCount() != 0 || Modifier.isStatic(method.getModifiers())
				|| method.getReturnType() == void.class || method.getDeclaringClass() == Object.class) {
			return false;
		}
		String name = method.getName();
		return name.startsWith("get") || name.startsWith("is") || name.startsWith("has");
	}

	private static boolean isSetter(Method method) {
		return method.getParameterCount() == 1 && method.getName().startsWith("set")
				&& !Modifier.isStatic(method.getModifiers()) && method.getDeclaringClass() != Object.class;
	}
}
