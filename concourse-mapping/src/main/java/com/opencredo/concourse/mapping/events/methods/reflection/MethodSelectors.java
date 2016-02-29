package com.opencredo.concourse.mapping.events.methods.reflection;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEvent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

final class MethodSelectors {

    private MethodSelectors() {
    }

    private static Predicate<Method> isAnnotatedWith(Class<? extends Annotation> annotation) {
        return method -> method.isAnnotationPresent(annotation);
    }

    private static final Predicate<Method> handlesEvent = isAnnotatedWith(HandlesEvent.class);

    private static final Predicate<Method> returnsVoid = method -> method.getReturnType().equals(void.class);
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

    static final Predicate<Method> isEventEmittingMethod =
            returnsVoid
                    .and(parameterCountGte(2))
                    .and(firstParameterIs(StreamTimestamp.class))
                    .and(secondParameterIs(UUID.class));

    static final Predicate<Method> isFactoryMethod = handlesEvent
            .and(isStatic)
            .and(parameterCountGte(1))
            .and(firstParameterIs(UUID.class));

    static final Predicate<Method> isUpdateMethod = handlesEvent.and(isInstance).and(returnsVoid);

    static <T, I> Map<Method, I> interpretMethods(Class<? extends T> klass, Predicate<Method> methodSelector, Function<Method, I> interpreterBuilder) {
        return Stream.of(klass.getMethods())
                .filter(methodSelector)
                .distinct()
                .collect(toMap(
                        Function.identity(),
                        interpreterBuilder
                ));
    }
}
