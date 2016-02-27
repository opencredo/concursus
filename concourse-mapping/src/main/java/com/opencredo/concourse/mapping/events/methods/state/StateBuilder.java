package com.opencredo.concourse.mapping.events.methods.state;

import com.opencredo.concourse.domain.events.sourcing.CachedEventSource;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.time.TimeRange;
import com.opencredo.concourse.mapping.events.methods.reflection.EventTypeBinding;
import com.opencredo.concourse.mapping.events.methods.reflection.StateClassInfo;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toMap;

public final class StateBuilder<T> {

    @SuppressWarnings("unchecked")
    public static <T> StateBuilder<T> forStateClass(Class<? extends T> stateClass) {
        StateClassInfo<T> stateClassInfo = StateClassInfo.forStateClass(stateClass);
        return new StateBuilder<>(
                stateClassInfo.getEventTypeBinding(),
                () -> StateMethodDispatcher.dispatching(
                        stateClassInfo.getInitialEventDispatcher(),
                        stateClassInfo.getUpdateEventDispatcher(),
                        stateClassInfo.getCausalOrder()));
    }

    private final EventTypeBinding typeBinding;
    private final Supplier<StateMethodDispatcher<T>> stateMethodDispatcherSupplier;

    private StateMethodDispatcher<T> createDispatcher() {
        return stateMethodDispatcherSupplier.get();
    }

    public StateBuilder(EventTypeBinding typeBinding, Supplier<StateMethodDispatcher<T>> stateMethodDispatcherSupplier) {
        this.typeBinding = typeBinding;
        this.stateMethodDispatcherSupplier = stateMethodDispatcherSupplier;
    }

    public Optional<T> buildState(EventSource eventSource, UUID aggregateId) {
        return buildState(eventSource, aggregateId, TimeRange.unbounded());
    }

    public Optional<T> buildState(EventSource eventSource, UUID aggregateId, TimeRange timeRange) {
        return createDispatcher().apply(typeBinding.replaying(eventSource, aggregateId, timeRange));
    }

    public Optional<T> buildState(CachedEventSource cachedEventSource, UUID aggregateId) {
        return buildState(cachedEventSource, aggregateId, TimeRange.unbounded());
    }

    public Optional<T> buildState(CachedEventSource cachedEventSource, UUID aggregateId, TimeRange timeRange) {
        return createDispatcher().apply(typeBinding.replaying(cachedEventSource, aggregateId, timeRange));
    }

    public CachedEventSource preload(EventSource eventSource, Collection<UUID> aggregateIds) {
        return preload(eventSource, aggregateIds, TimeRange.unbounded());
    }

    public CachedEventSource preload(EventSource eventSource, Collection<UUID> aggregateIds, TimeRange timeRange) {
        return typeBinding.preload(eventSource, aggregateIds, timeRange);
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

}
