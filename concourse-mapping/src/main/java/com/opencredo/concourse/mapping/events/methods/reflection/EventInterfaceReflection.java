package com.opencredo.concourse.mapping.events.methods.reflection;

import com.opencredo.concourse.data.tuples.TupleSchemaRegistry;
import com.opencredo.concourse.domain.common.VersionedName;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.annotations.Name;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    private static final ConcurrentMap<Class<?>, Map<Method, EventMethodMapping>> cache = new ConcurrentHashMap<>();

    public static Map<Method, EventMethodMapping> getEventMappers(Class<?> klass) {
        return cache.computeIfAbsent(klass, EventInterfaceReflection::getEventMappersUncached);
    }

    private static Map<Method, EventMethodMapping> getEventMappersUncached(Class<?> klass) {
        return Stream.of(klass.getMethods())
                .filter(EventInterfaceReflection::isEventEmittingMethod)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        EventMethodMapping::forMethod
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
        Map<Method, EventMethodMapping> methodMappings = getEventMappers(klass);

        return methodMappings.entrySet().stream().collect(Collectors.toMap(
                e -> e.getValue().getEventType(),
                e -> getEventDispatcher(e.getKey(), e.getValue())
        ));
    }

    private static BiConsumer<Object, Event> getEventDispatcher(Method method, EventMethodMapping eventMethodMapping) {
        return (target, event) -> {
            try {
                method.invoke(target, eventMethodMapping.mapEvent(event));
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static final ConcurrentHashMap<Class<?>, EventTypeMatcher> eventTypeMatcherCache = new ConcurrentHashMap<>();

    public static EventTypeMatcher getEventTypeMatcher(Class<?> handlerClass) {
        return eventTypeMatcherCache.computeIfAbsent(handlerClass, EventInterfaceReflection::getEventTypeMatcherUncached);
    }

    private static EventTypeMatcher getEventTypeMatcherUncached(Class<?> handlerClass) {
        TupleSchemaRegistry registry = new TupleSchemaRegistry();
        getEventMappers(handlerClass).values().stream().forEach(mapper -> mapper.registerSchema(registry));
        return EventTypeMatcher.matchingAgainst(registry);
    }
}
