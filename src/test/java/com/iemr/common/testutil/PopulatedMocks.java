package com.iemr.common.testutil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;

import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

/**
 * Creates collaborator mocks that answer with populated results instead of nulls.
 *
 * <p>The wide services in this code base take dozens of repositories and mappers and then
 * dereference whatever comes back. Handing them mocks that return fully built objects lets a
 * test drive the real branches without stubbing every lookup by hand.
 */
public final class PopulatedMocks {

	private PopulatedMocks() {
	}

	/** Answers every call with a populated instance of its return type. */
	public static final Answer<Object> POPULATED_RESULTS = invocation -> {
		Method method = invocation.getMethod();
		Class<?> returnType = method.getReturnType();
		if (returnType == void.class) {
			return null;
		}
		// Repository save() and friends are generic, so their erased return type is Object;
		// echoing the argument back is what the real call does.
		if (returnType == Object.class && invocation.getArguments().length == 1) {
			return invocation.getArgument(0);
		}
		Object value = ReflectiveFiller.build(returnType, method.getGenericReturnType(), 0, new ArrayDeque<>());
		if (value == null && returnType.isPrimitive()) {
			return Mockito.RETURNS_DEFAULTS.answer(invocation);
		}
		return value;
	};

	/** A mock of {@code type} whose calls answer with populated results. */
	public static <T> T of(Class<T> type) {
		return Mockito.mock(type, Mockito.withSettings().defaultAnswer(POPULATED_RESULTS));
	}

	/**
	 * Fills every non-primitive, non-String instance field of {@code target} that is still
	 * null with a populated mock, leaving anything the test has already set in place.
	 */
	public static void injectCollaborators(Object target) {
		for (Class<?> current = target.getClass(); current != null
				&& current != Object.class; current = current.getSuperclass()) {
			for (Field field : current.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())
						|| field.getType().isPrimitive() || field.getType() == String.class
						|| field.getType().isEnum() || field.getType().getName().startsWith("java.")) {
					continue;
				}
				try {
					field.setAccessible(true);
					if (field.get(target) != null) {
						continue;
					}
					field.set(target, of(field.getType()));
				} catch (Throwable ignored) {
					// a collaborator that cannot be mocked is left null
				}
			}
		}
	}
}
