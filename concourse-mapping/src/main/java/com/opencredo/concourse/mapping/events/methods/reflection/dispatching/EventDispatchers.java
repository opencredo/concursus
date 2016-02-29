package com.opencredo.concourse.mapping.events.methods.reflection.dispatching;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.EventMethodMapping;
import com.opencredo.concourse.mapping.reflection.MethodInvoking;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.*;
import static java.util.stream.Collectors.toMap;

public final class EventDispatchers {

    private EventDispatchers() {
    }

    public static <T> InitialEventDispatcher<T> toFactoryMethod(Class<? extends T> targetClass, Method method, EventMethodMapping methodInfo) {
        return event -> MethodInvoking.invokingStatic(targetClass, method).apply(methodInfo.mapEvent(event));
    }

    public static <T> EventDispatcher<T> toUpdateOrEmitterMethod(Method method, EventMethodMapping methodInfo) {
        return (target, event) -> MethodInvoking.invokingInstance(method, target).apply(methodInfo.mapEvent(event));
    }

    public static <T> InitialEventDispatcher<T> dispatchingInitialEventsByType(Class<? extends T> stateClass, Map<Method, EventMethodMapping> factoryMethodInfo) {
        return new TypeMappingInitialEventDispatcher<T>(makeEventTypeMap(
                factoryMethodInfo,
                (method, interpreter) -> toFactoryMethod(stateClass, method, interpreter)));
    }

    public static <T> MultiTypeEventDispatcher<T> dispatchingEventsByType(Map<Method, EventMethodMapping> updateMethodInterpreters) {
        return new TypeMappingEventDispatcher<>(makeEventTypeMap(updateMethodInterpreters, EventDispatchers::toUpdateOrEmitterMethod));
    }

    private static <T, D> Map<EventType, D> makeEventTypeMap(Map<Method, EventMethodMapping> interpreterMap, BiFunction<Method, EventMethodMapping, D> dispatcherBuilder) {
        return interpreterMap.entrySet().stream().collect(toMap(
                e -> e.getValue().getEventType(),
                e -> dispatcherBuilder.apply(e.getKey(), e.getValue())
        ));
    }

    private static final class TypeMappingEventDispatcher<T> implements MultiTypeEventDispatcher<T> {

        private final Map<EventType, EventDispatcher<T>> eventDispatchers;

        private TypeMappingEventDispatcher(Map<EventType, EventDispatcher<T>> eventDispatchers) {
            this.eventDispatchers = eventDispatchers;
        }

        @Override
        public void accept(T target, Event event) {
            checkNotNull(event, "event must not be null");

            final EventDispatcher<T> dispatcher = eventDispatchers.get(EventType.of(event));
            checkState(dispatcher != null,
                    "No dispatcher found for event " + event);

            dispatcher.accept(target, event);
        }

        @Override
        public Set<EventType> getHandledEventTypes() {
            return eventDispatchers.keySet();
        }
    }

    private static final class TypeMappingInitialEventDispatcher<T> implements InitialEventDispatcher<T> {

        private final Map<EventType, InitialEventDispatcher<T>> eventDispatchers;

        private TypeMappingInitialEventDispatcher(Map<EventType, InitialEventDispatcher<T>> eventDispatchers) {
            this.eventDispatchers = eventDispatchers;
        }

        @Override
        public T apply(Event event) {
            checkNotNull(event, "event must not be null");

            InitialEventDispatcher<T> dispatcher = eventDispatchers.get(EventType.of(event));
            checkArgument(dispatcher != null,
                    "No dispatcher found for initial event %s", event);

            return dispatcher.apply(event);
        }
    }
}
