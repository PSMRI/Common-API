package com.iemr.common.testutil;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.objenesis.ObjenesisStd;

/**
 * Builds fully-populated instances of arbitrary POJOs by reflection.
 *
 * <p>Used by the sweep tests to exercise entity/DTO accessors and to drive the
 * MapStruct generated mappers down their non-null branches. Every operation is
 * best-effort: anything that cannot be built yields {@code null} rather than an
 * exception, so a single awkward class never breaks a sweep.
 */
public final class ReflectiveFiller {

	private static final int MAX_DEPTH = 4;
	private static final AtomicInteger SEQ = new AtomicInteger(1);
	private static final ObjenesisStd OBJENESIS = new ObjenesisStd();

	private ReflectiveFiller() {
	}

	/** Builds a populated instance of {@code type}, or {@code null} if it cannot be built. */
	@SuppressWarnings("unchecked")
	public static <T> T fill(Class<T> type) {
		return (T) build(type, null, 0, new ArrayDeque<>());
	}

	/** Builds an instance of {@code type} with every field left at its default (usually null). */
	@SuppressWarnings("unchecked")
	public static <T> T empty(Class<T> type) {
		return (T) instantiate(type, 0, new ArrayDeque<>());
	}

	public static Object build(Class<?> type, Type generic, int depth, Deque<Class<?>> stack) {
		if (type == null) {
			return null;
		}
		Object scalar = scalar(type);
		if (scalar != NOT_SCALAR) {
			return scalar;
		}
		if (type.isArray()) {
			return buildArray(type, depth, stack);
		}
		if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)
				|| Optional.class.equals(type)) {
			return buildContainer(type, generic, depth, stack);
		}
		if (depth >= MAX_DEPTH || stack.contains(type) || type.isInterface()
				|| Modifier.isAbstract(type.getModifiers())) {
			return null;
		}
		stack.push(type);
		try {
			Object instance = instantiate(type, depth, stack);
			if (instance != null) {
				populate(instance, type, depth, stack);
			}
			return instance;
		} finally {
			stack.pop();
		}
	}

	private static Object buildArray(Class<?> type, int depth, Deque<Class<?>> stack) {
		Class<?> component = type.getComponentType();
		Object array = Array.newInstance(component, 1);
		Object element = build(component, null, depth + 1, stack);
		if (element != null) {
			Array.set(array, 0, element);
		}
		return array;
	}

	private static Object buildContainer(Class<?> type, Type generic, int depth, Deque<Class<?>> stack) {
		Type[] args = generic instanceof ParameterizedType ? ((ParameterizedType) generic).getActualTypeArguments()
				: new Type[0];
		if (Map.class.isAssignableFrom(type)) {
			Map<Object, Object> map = type.isInterface() || Modifier.isAbstract(type.getModifiers())
					? new LinkedHashMap<>()
					: asMap(instantiate(type, depth, stack));
			if (map == null) {
				map = new LinkedHashMap<>();
			}
			Object key = args.length > 0 ? build(raw(args[0]), args[0], depth + 1, stack) : "key" + next();
			Object value = args.length > 1 ? build(raw(args[1]), args[1], depth + 1, stack) : "value" + next();
			if (key != null) {
				map.put(key, value);
			}
			return map;
		}
		if (Optional.class.equals(type)) {
			Object value = args.length > 0 ? build(raw(args[0]), args[0], depth + 1, stack) : null;
			return Optional.ofNullable(value);
		}
		Collection<Object> collection = Set.class.isAssignableFrom(type) ? new LinkedHashSet<>() : new ArrayList<>();
		Object element = args.length > 0 ? build(raw(args[0]), args[0], depth + 1, stack) : null;
		if (element != null) {
			collection.add(element);
		}
		return collection;
	}

	@SuppressWarnings("unchecked")
	private static Map<Object, Object> asMap(Object candidate) {
		return candidate instanceof Map ? (Map<Object, Object>) candidate : null;
	}

	private static Class<?> raw(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		}
		if (type instanceof ParameterizedType) {
			return raw(((ParameterizedType) type).getRawType());
		}
		return Object.class;
	}

	private static Object instantiate(Class<?> type, int depth, Deque<Class<?>> stack) {
		Constructor<?>[] constructors = type.getDeclaredConstructors();
		List<Constructor<?>> ordered = new ArrayList<>(List.of(constructors));
		ordered.sort((a, b) -> Integer.compare(a.getParameterCount(), b.getParameterCount()));
		for (Constructor<?> constructor : ordered) {
			try {
				constructor.setAccessible(true);
				Class<?>[] parameterTypes = constructor.getParameterTypes();
				Type[] parameterGenerics = constructor.getGenericParameterTypes();
				Object[] arguments = new Object[parameterTypes.length];
				for (int i = 0; i < parameterTypes.length; i++) {
					arguments[i] = build(parameterTypes[i], parameterGenerics[i], depth + 1, stack);
					if (arguments[i] == null && parameterTypes[i].isPrimitive()) {
						arguments[i] = scalar(parameterTypes[i]);
					}
				}
				return constructor.newInstance(arguments);
			} catch (Throwable ignored) {
				// try the next constructor
			}
		}
		try {
			return OBJENESIS.newInstance(type);
		} catch (Throwable ignored) {
			return null;
		}
	}

	/** Invokes {@code constructor} with reflectively built arguments, or returns null if it fails. */
	public static Object construct(Constructor<?> constructor) {
		try {
			constructor.setAccessible(true);
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			Type[] parameterGenerics = constructor.getGenericParameterTypes();
			Object[] arguments = new Object[parameterTypes.length];
			for (int i = 0; i < parameterTypes.length; i++) {
				arguments[i] = build(parameterTypes[i], parameterGenerics[i], 1, new ArrayDeque<>());
				if (arguments[i] == null && parameterTypes[i].isPrimitive()) {
					arguments[i] = scalar(parameterTypes[i]);
				}
			}
			return constructor.newInstance(arguments);
		} catch (Throwable ignored) {
			return null;
		}
	}

	/** Invokes {@code method} on {@code target} with reflectively built arguments. */
	public static Object invokeWithFilledArguments(Method method, Object target) {
		try {
			method.setAccessible(true);
			Class<?>[] parameterTypes = method.getParameterTypes();
			Type[] parameterGenerics = method.getGenericParameterTypes();
			Object[] arguments = new Object[parameterTypes.length];
			for (int i = 0; i < parameterTypes.length; i++) {
				arguments[i] = build(parameterTypes[i], parameterGenerics[i], 1, new ArrayDeque<>());
				if (arguments[i] == null && parameterTypes[i].isPrimitive()) {
					arguments[i] = scalar(parameterTypes[i]);
				}
			}
			return method.invoke(target, arguments);
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
	}

	private static void populate(Object instance, Class<?> type, int depth, Deque<Class<?>> stack) {
		Map<String, Method> setters = setters(type);
		for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
			for (Field field : current.getDeclaredFields()) {
				if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				Object value = build(field.getType(), field.getGenericType(), depth + 1, stack);
				if (value == null && !field.getType().isPrimitive()) {
					continue;
				}
				Method setter = setters.get(setterName(field.getName()));
				if (setter != null && setter.getParameterTypes()[0].isAssignableFrom(box(field.getType()))) {
					try {
						setter.invoke(instance, value);
						continue;
					} catch (Throwable ignored) {
						// fall through to direct field assignment
					}
				}
				if (Modifier.isFinal(field.getModifiers())) {
					continue;
				}
				try {
					field.setAccessible(true);
					field.set(instance, value);
				} catch (Throwable ignored) {
					// leave the field at its default
				}
			}
		}
	}

	private static Map<String, Method> setters(Class<?> type) {
		Map<String, Method> result = new HashMap<>();
		for (Method method : type.getMethods()) {
			if (method.getParameterCount() == 1 && method.getName().startsWith("set")
					&& !Modifier.isStatic(method.getModifiers())) {
				result.putIfAbsent(method.getName(), method);
			}
		}
		return result;
	}

	private static String setterName(String fieldName) {
		if (fieldName.isEmpty()) {
			return "set";
		}
		return "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
	}

	private static Class<?> box(Class<?> type) {
		if (!type.isPrimitive()) {
			return type;
		}
		if (type == int.class) {
			return Integer.class;
		}
		if (type == long.class) {
			return Long.class;
		}
		if (type == short.class) {
			return Short.class;
		}
		if (type == byte.class) {
			return Byte.class;
		}
		if (type == double.class) {
			return Double.class;
		}
		if (type == float.class) {
			return Float.class;
		}
		if (type == boolean.class) {
			return Boolean.class;
		}
		if (type == char.class) {
			return Character.class;
		}
		return type;
	}

	private static final Object NOT_SCALAR = new Object();

	private static int next() {
		return SEQ.getAndIncrement() % 97 + 1;
	}

	private static Object scalar(Class<?> type) {
		int seq = 0;
		if (type == String.class) {
			// A digit string keeps mappers that run the value through Integer/Long/Double
			// parsing on their happy path, while still reading fine as free text.
			return String.valueOf(100000000 + next());
		}
		if (type == int.class || type == Integer.class) {
			return next();
		}
		if (type == long.class || type == Long.class) {
			return (long) next();
		}
		if (type == short.class || type == Short.class) {
			return (short) next();
		}
		if (type == byte.class || type == Byte.class) {
			return (byte) next();
		}
		if (type == double.class || type == Double.class) {
			return next() + 0.5d;
		}
		if (type == float.class || type == Float.class) {
			return next() + 0.5f;
		}
		if (type == boolean.class || type == Boolean.class) {
			return Boolean.TRUE;
		}
		if (type == char.class || type == Character.class) {
			return 'A';
		}
		if (type == BigDecimal.class) {
			return BigDecimal.valueOf(next());
		}
		if (type == BigInteger.class) {
			return BigInteger.valueOf(next());
		}
		if (type == java.util.Date.class) {
			return new java.util.Date(1_700_000_000_000L + next());
		}
		if (type == java.sql.Date.class) {
			return new java.sql.Date(1_700_000_000_000L + next());
		}
		if (type == Timestamp.class) {
			return new Timestamp(1_700_000_000_000L + next());
		}
		if (type == Time.class) {
			return new Time(1_000_000L + next());
		}
		if (type == LocalDate.class) {
			return LocalDate.of(2024, 1, 1).plusDays(next());
		}
		if (type == LocalDateTime.class) {
			return LocalDateTime.of(2024, 1, 1, 10, 30).plusMinutes(next());
		}
		if (type == LocalTime.class) {
			return LocalTime.of(10, 30).plusMinutes(next());
		}
		if (type == ZonedDateTime.class) {
			return ZonedDateTime.of(LocalDateTime.of(2024, 1, 1, 10, 30), java.time.ZoneOffset.UTC);
		}
		if (type == OffsetDateTime.class) {
			return OffsetDateTime.of(LocalDateTime.of(2024, 1, 1, 10, 30), java.time.ZoneOffset.UTC);
		}
		if (type == UUID.class) {
			return UUID.nameUUIDFromBytes(String.valueOf(next()).getBytes());
		}
		if (type == Locale.class) {
			return Locale.ENGLISH;
		}
		if (type == Currency.class) {
			return Currency.getInstance("INR");
		}
		if (type == Class.class) {
			return String.class;
		}
		if (type == Object.class) {
			return String.valueOf(400000000 + next());
		}
		if (type == CharSequence.class) {
			return String.valueOf(500000000 + next());
		}
		if (type == StringBuilder.class) {
			return new StringBuilder(String.valueOf(600000000 + next()));
		}
		if (type.isEnum()) {
			Object[] constants = type.getEnumConstants();
			return constants != null && constants.length > 0 ? constants[seq] : null;
		}
		return NOT_SCALAR;
	}
}
