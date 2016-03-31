package com.opencredo.concursus.mapping.reflection;

import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesEvent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public final class MethodSelectors {

    private MethodSelectors() {
    }

    private static Predicate<Method> isAnnotatedWith(Class<? extends Annotation> annotation) {
        return method -> method.isAnnotationPresent(annotation);
    }

    private static final Predicate<Method> handlesEvent = isAnnotatedWith(HandlesEvent.class);

    private static Predicate<Method> returnsType(Class<?> returnType) {
        return method -> method.getReturnType().equals(returnType);
    }

    private static final Predicate<Method> returnsVoid = returnsType(void.class);
    private static final Predicate<Method> returnsCompletableFuture = returnsType(CompletableFuture.class);

    private static final Predicate<Method> isStatic = method -> Modifier.isStatic(method.getModifiers());

    private static final Predicate<Method> isInstance = isStatic.negate();

    private static Predicate<Method> parameterCountGte(int minParameterCount) {
        return method -> method.getParameterCount() >= minParameterCount;
    }

    private static Predicate<Method> hasParameterType(int index, Class<?> expected) {
        return method -> method.getParameterTypes()[index].equals(expected);
    }

    private static Predicate<Method> firstParameterIs(Class<?> expected) {
        return hasParameterType(0, expected);
    }

    private static Predicate<Method> secondParameterIs(Class<?> expected) {
        return hasParameterType(1, expected);
    }

    private static Predicate<Method> hasTimestampAndUUIDParameters = parameterCountGte(2)
            .and(firstParameterIs(StreamTimestamp.class))
            .and(secondParameterIs(UUID.class));

    public static final Predicate<Method> isEventEmittingMethod =
            returnsVoid.and(isInstance).and(hasTimestampAndUUIDParameters);

    public static final Predicate<Method> isFactoryMethod = handlesEvent
            .and(isStatic)
            .and(parameterCountGte(1))
            .and(firstParameterIs(UUID.class));

    public static final Predicate<Method> isUpdateMethod = handlesEvent.and(isInstance).and(returnsVoid);

    public static final Predicate<Method> isCommandIssuingMethod =
            returnsCompletableFuture.and(isInstance).and(hasTimestampAndUUIDParameters);

}
