package com.opencredo.concourse.mapping.methods;

import com.opencredo.concourse.domain.StreamTimestamp;
import com.opencredo.concourse.domain.VersionedName;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.annotations.Name;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EventInterfaceReflection {
    public static String getAggregateType(Class<?> klass) {
        return klass.getAnnotation(HandlesEventsFor.class).value();
    }

    public static VersionedName getEventName(Method method) {
        return method.isAnnotationPresent(Name.class)
                ? VersionedName.of(
                method.getAnnotation(Name.class).value(),
                method.getAnnotation(Name.class).version())
                : VersionedName.of(method.getName(), "0");
    }

    public static String getParameterName(Parameter parameter) {
        return parameter.isAnnotationPresent(Name.class)
                ? parameter.getAnnotation(Name.class).value()
                : parameter.getName();
    }

    private static final ConcurrentMap<Class<?>, Map<Method, MethodMapping>> cache = new ConcurrentHashMap<>();

    public static Map<Method, MethodMapping> getEventMappers(Class<?> klass) {
        return cache.computeIfAbsent(klass, EventInterfaceReflection::getEventMappersUncached);
    }

    private static Map<Method, MethodMapping> getEventMappersUncached(Class<?> klass) {
        return Stream.of(klass.getMethods())
                .filter(EventInterfaceReflection::isEventEmittingMethod)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        MethodMapping::forMethod
                ));
    }

    private static boolean isEventEmittingMethod(Method method) {
        return method.getReturnType().equals(void.class)
                && method.getParameterCount() >= 2
                && method.getParameterTypes()[0].equals(StreamTimestamp.class)
                && method.getParameterTypes()[1].equals(UUID.class);
    }

    private static final ConcurrentMap<Class<?>, Map<EventType, BiConsumer<Object, Event>>> cache2 = new ConcurrentHashMap<>();

    public static Map<EventType, BiConsumer<Object, Event>> getEventDispatchers(Class<?> klass) {
        return cache2.computeIfAbsent(klass, EventInterfaceReflection::getEventDispatchersUncached);
    }

    public static Map<EventType, BiConsumer<Object, Event>> getEventDispatchersUncached(Class<?> klass) {
        Map<Method, MethodMapping> methodMappings = getEventMappers(klass);

        return methodMappings.entrySet().stream().collect(Collectors.toMap(
                e -> e.getValue().getEventType(),
                e -> getEventDispatcher(e.getKey(), e.getValue())
        ));
    }

    private static BiConsumer<Object, Event> getEventDispatcher(Method method, MethodMapping methodMapping) {
        return (target, event) -> {
            try {
                method.invoke(target, methodMapping.mapEvent(event));
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
