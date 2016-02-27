package com.opencredo.concourse.mapping.events.methods.reflection;

import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.mapping.annotations.HandlesEvent;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.events.methods.ordering.CausalOrdering;
import com.opencredo.concourse.mapping.events.methods.reflection.dispatching.*;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.EventInterpreters;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.TypeMappingEventInterpreter;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.api.TypeMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toMap;

public final class StateClassInfo<T> {

    public static <T> StateClassInfo<T> forStateClass(Class<? extends T> stateClass) {
        checkNotNull(stateClass, "stateClass must not be null");
        checkArgument(stateClass.isAnnotationPresent(HandlesEventsFor.class));

        String aggregateType = stateClass.getAnnotation(HandlesEventsFor.class).value();

        Map<Method, TypeMappingEventInterpreter> factoryMethodMappings = getFactoryMethodMappings(stateClass, aggregateType);
        Map<Method, TypeMappingEventInterpreter> updateMethodMappings = getUpdateMethodMappings(stateClass, aggregateType);

        EventTypeMatcher eventTypeMatcher = getEventTypeMatcher(factoryMethodMappings.values(), updateMethodMappings.values());

        final Comparator<Event> causalOrder = getCausalOrder(factoryMethodMappings.values(), updateMethodMappings.values());
        final InitialEventDispatcher<T> initialEventDispatcher = getInitialEventDispatcher(stateClass, factoryMethodMappings);
        final EventDispatcher<T> updateEventDispatcher = getUpdateEventDispatcher(updateMethodMappings);

        return new StateClassInfo<>(
                EventTypeBinding.of(aggregateType, eventTypeMatcher),
                causalOrder,
                initialEventDispatcher,
                updateEventDispatcher);
    }

    private static <T> Map<Method, TypeMappingEventInterpreter> getFactoryMethodMappings(Class<? extends T> stateClass, String aggregateType) {
        return Stream.of(stateClass.getMethods())
                .filter(StateClassInfo::isFactoryMethod)
                .distinct()
                .collect(toMap(
                        Function.identity(),
                        method -> EventInterpreters.forFactoryMethod(stateClass, aggregateType, method)
                ));
    }

    private static <T> Map<Method, TypeMappingEventInterpreter> getUpdateMethodMappings(Class<? extends T> stateClass, String aggregateType) {
        return Stream.of(stateClass.getMethods())
                .filter(StateClassInfo::isUpdateMethod)
                .distinct()
                .collect(toMap(
                        Function.identity(),
                        method -> EventInterpreters.forUpdateMethod(stateClass, aggregateType, method)
                ));
    }

    private static boolean isFactoryMethod(Method method) {
        return Modifier.isStatic(method.getModifiers())
                && method.isAnnotationPresent(HandlesEvent.class);
    }

    private static boolean isUpdateMethod(Method method) {
        return !Modifier.isStatic(method.getModifiers())
                && method.isAnnotationPresent(HandlesEvent.class);
    }

    private static <T> InitialEventDispatcher<T> getInitialEventDispatcher(Class<? extends T> stateClass, Map<Method, TypeMappingEventInterpreter> factoryMethodInterpreters) {
        return TypeMappingInitialEventDispatcher.mapping(factoryMethodInterpreters.entrySet().stream()
            .collect(toMap(
                    e -> e.getValue().getEventType(),
                    e -> FactoryMethodInvokingInitialEventDispatcher.dispatching(stateClass, e.getKey(), e.getValue()))));
    }

    private static <T> EventDispatcher<T> getUpdateEventDispatcher(Map<Method, TypeMappingEventInterpreter> updateMethodInterpreters) {
        return TypeMappingEventDispatcher.mapping(updateMethodInterpreters.entrySet().stream()
            .collect(toMap(
                    e -> e.getValue().getEventType(),
                    e -> MethodInvokingEventDispatcher.dispatching(e.getKey(), e.getValue())
            )));
    }

    private static <T> EventTypeMatcher getEventTypeMatcher(
            Collection<? extends TypeMapping> factoryMethodMappings,
            Collection<? extends TypeMapping> updateMethodMappings) {
        Map<EventType, TupleSchema> factoryMappings = factoryMethodMappings.stream()
                .collect(toMap(TypeMapping::getEventType, TypeMapping::getTupleSchema));

        Map<EventType, TupleSchema> updateMappings = updateMethodMappings.stream()
                .collect(toMap(TypeMapping::getEventType, TypeMapping::getTupleSchema));

        updateMappings.putAll(factoryMappings);
        return EventTypeMatcher.matchingAgainst(updateMappings);
    }

    private static <T> Comparator<Event> getCausalOrder(
            Collection<? extends TypeMapping> factoryMethodMappings,
            Collection<? extends TypeMapping> updateMethodMappings) {

        Map<EventType, Integer> ordering = factoryMethodMappings.stream()
                .collect(toMap(TypeMapping::getEventType, TypeMapping::getCausalOrder));

        updateMethodMappings.forEach(mapping ->
                ordering.put(mapping.getEventType(), mapping.getCausalOrder()));

        return CausalOrdering.onEventTypes(ordering);
    }

    private final EventTypeBinding eventTypeBinding;
    private final Comparator<Event> causalOrder;
    private final InitialEventDispatcher<T> initialEventDispatcher;
    private final EventDispatcher<T> updateEventDispatcher;

    public StateClassInfo(EventTypeBinding eventTypeBinding, Comparator<Event> causalOrder, InitialEventDispatcher<T> initialEventDispatcher, EventDispatcher<T> updateEventDispatcher) {
        this.eventTypeBinding = eventTypeBinding;
        this.causalOrder = causalOrder;
        this.initialEventDispatcher = initialEventDispatcher;
        this.updateEventDispatcher = updateEventDispatcher;
    }

    public EventTypeBinding getEventTypeBinding() {
        return eventTypeBinding;
    }

    public Comparator<Event> getCausalOrder() {
        return causalOrder;
    }

    public InitialEventDispatcher<T> getInitialEventDispatcher() {
        return initialEventDispatcher;
    }

    public EventDispatcher<T> getUpdateEventDispatcher() {
        return updateEventDispatcher;
    }
}
