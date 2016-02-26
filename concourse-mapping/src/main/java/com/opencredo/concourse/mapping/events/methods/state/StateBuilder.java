package com.opencredo.concourse.mapping.events.methods.state;

import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.domain.events.sourcing.CachedEventSource;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.time.TimeRange;
import com.opencredo.concourse.mapping.annotations.HandlesEvent;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toMap;

public final class StateBuilder<T> {

    private static final ConcurrentMap<Class<?>, StateBuilder<?>> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> StateBuilder<T> forStateClass(Class<? extends T> stateClass) {
        return (StateBuilder<T>) cache.computeIfAbsent(stateClass, StateBuilder::forStateClassUncached);
    }

    private static <T> StateBuilder<T> forStateClassUncached(Class<? extends T> stateClass) {
        checkNotNull(stateClass, "stateClass must not be null");
        checkArgument(stateClass.isAnnotationPresent(HandlesEventsFor.class));

        String aggregateType = stateClass.getAnnotation(HandlesEventsFor.class).value();

        Map<EventType, StateFactoryMethodDispatcher<T>> factoryMethodDispatchers = getStateFactoryMethodDispatchers(stateClass, aggregateType);
        Map<EventType, StateUpdateMethodDispatcher> updateMethodDispatchers = getStateUpdateMethodDispatchers(stateClass, aggregateType);

        EventTypeMatcher eventTypeMatcher = getEventTypeMatcher(factoryMethodDispatchers, updateMethodDispatchers);
        Comparator<Event> causalOrder = getCausalOrder(factoryMethodDispatchers, updateMethodDispatchers);

        return new StateBuilder<>(
                aggregateType,
                eventTypeMatcher,
                causalOrder,
                factoryMethodDispatchers,
                updateMethodDispatchers);
    }

    private static <T> Map<EventType, StateFactoryMethodDispatcher<T>> getStateFactoryMethodDispatchers(Class<? extends T> stateClass, String aggregateType) {
        return Stream.of(stateClass.getMethods())
                .filter(StateBuilder::isFactoryMethod)
                .map(method -> StateFactoryMethodDispatcher.of(stateClass, aggregateType, method))
                .collect(toMap(
                        StateFactoryMethodDispatcher::getEventType,
                        Function.identity()
                ));
    }

    private static Map<EventType, StateUpdateMethodDispatcher> getStateUpdateMethodDispatchers(Class<?> stateClass, String aggregateType) {
        return Stream.of(stateClass.getMethods())
                .filter(StateBuilder::isUpdateMethod)
                .map(method -> StateUpdateMethodDispatcher.of(stateClass, aggregateType, method))
                .collect(toMap(
                        StateUpdateMethodDispatcher::getEventType,
                        Function.identity()
                ));
    }

    private static <T> EventTypeMatcher getEventTypeMatcher(
            Map<EventType, StateFactoryMethodDispatcher<T>> factoryMethodDispatchers,
            Map<EventType, StateUpdateMethodDispatcher> updateMethodDispatchers) {
        Map<EventType, TupleSchema> factoryMappings = factoryMethodDispatchers.values().stream()
                .collect(toMap(StateFactoryMethodDispatcher::getEventType, StateFactoryMethodDispatcher::getTupleSchema));
        Map<EventType, TupleSchema> updateMappings = updateMethodDispatchers.values().stream()
                .collect(toMap(StateUpdateMethodDispatcher::getEventType, StateUpdateMethodDispatcher::getTupleSchema));
        updateMappings.putAll(factoryMappings);
        return EventTypeMatcher.matchingAgainst(updateMappings);
    }

    private static <T> Comparator<Event> getCausalOrder(
            Map<EventType, StateFactoryMethodDispatcher<T>> factoryMethodDispatchers,
            Map<EventType, StateUpdateMethodDispatcher> updateMethodDispatchers) {

            return Comparator.comparing((Event event) ->
                    factoryMethodDispatchers.keySet().contains(EventType.of(event))
                        ? -1
                        : updateMethodDispatchers.get(EventType.of(event)).getCausalOrder())
                .thenComparing(Event::getEventTimestamp);
    }

    private static boolean isFactoryMethod(Method method) {
        return Modifier.isStatic(method.getModifiers())
                && method.isAnnotationPresent(HandlesEvent.class);
    }

    private static boolean isUpdateMethod(Method method) {
        return !Modifier.isStatic(method.getModifiers())
                && method.isAnnotationPresent(HandlesEvent.class);
    }

    private final String aggregateType;
    private final EventTypeMatcher eventTypeMatcher;
    private final Comparator<Event> causalOrder;
    private final Map<EventType, StateFactoryMethodDispatcher<T>> factoryMethodDispatchers;
    private final Map<EventType, StateUpdateMethodDispatcher> updateMethodDispatchers;

    private StateBuilder(String aggregateType, EventTypeMatcher eventTypeMatcher, Comparator<Event> causalOrder, Map<EventType, StateFactoryMethodDispatcher<T>> factoryMethodDispatchers, Map<EventType, StateUpdateMethodDispatcher> updateMethodDispatchers) {
        this.aggregateType = aggregateType;
        this.eventTypeMatcher = eventTypeMatcher;
        this.causalOrder = causalOrder;
        this.factoryMethodDispatchers = factoryMethodDispatchers;
        this.updateMethodDispatchers = updateMethodDispatchers;
    }

    public StateMethodDispatcher<T> getDispatcher() {
        return new StateMethodDispatcher<>(factoryMethodDispatchers, updateMethodDispatchers, causalOrder);
    }

    public Optional<T> buildState(EventSource eventSource, UUID aggregateId) {
        return buildState(eventSource, aggregateId, TimeRange.unbounded());
    }

    public Optional<T> buildState(EventSource eventSource, UUID aggregateId, TimeRange timeRange) {
        return getDispatcher().apply(eventSource.replaying(eventTypeMatcher, addTypeTo(aggregateId), timeRange));
    }

    public Optional<T> buildState(CachedEventSource cachedEventSource, UUID aggregateId) {
        return buildState(cachedEventSource, aggregateId, TimeRange.unbounded());
    }

    public Optional<T> buildState(CachedEventSource cachedEventSource, UUID aggregateId, TimeRange timeRange) {
        return getDispatcher().apply(cachedEventSource.replaying(addTypeTo(aggregateId), timeRange));
    }

    public CachedEventSource preload(EventSource eventSource, Collection<UUID> aggregateIds) {
        return preload(eventSource, aggregateIds, TimeRange.unbounded());
    }

    public CachedEventSource preload(EventSource eventSource, Collection<UUID> aggregateIds, TimeRange timeRange) {
        return eventSource.preload(eventTypeMatcher, aggregateType, aggregateIds, timeRange);
    }

    public Map<UUID, T> buildStates(EventSource eventSource, Collection<UUID> aggregateIds) {
        return buildStates(eventSource, aggregateIds, TimeRange.unbounded());
    }

    public Map<UUID, T> buildStates(EventSource eventSource, Collection<UUID> aggregateIds, TimeRange timeRange) {
        return buildStates(preload(eventSource, aggregateIds, timeRange), aggregateIds);
    }

    public Map<UUID, T> buildStates(CachedEventSource cachedEventSource, Collection<UUID> aggregateIds) {
        return buildStates(cachedEventSource, aggregateIds, TimeRange.unbounded());
    }

    public Map<UUID, T> buildStates(CachedEventSource cachedEventSource, Collection<UUID> aggregateIds, TimeRange timeRange) {
        return aggregateIds.stream()
                .map(id -> new SimpleEntry<>(id, buildState(cachedEventSource, id)))
                .filter(e -> e.getValue().isPresent())
                .collect(toMap(Entry::getKey, e -> e.getValue().get()));
    }

    private AggregateId addTypeTo(UUID aggregateId) {
        return AggregateId.of(aggregateType, aggregateId);
    }

}
