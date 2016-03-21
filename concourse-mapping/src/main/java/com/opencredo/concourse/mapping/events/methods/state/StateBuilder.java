package com.opencredo.concourse.mapping.events.methods.state;

import com.opencredo.concourse.domain.events.sourcing.CachedEventSource;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.time.TimeRange;
import com.opencredo.concourse.domain.events.binding.EventTypeBinding;
import com.opencredo.concourse.mapping.events.methods.reflection.StateClassInfo;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toMap;

/**
 * Builds a state object by replaying events from an {@link EventSource} and dispatching them to state class methods.
 * @param <T> The type of the state object to build.
 */
public final class StateBuilder<T> {

    /**
     * Create a {@link StateBuilder} for the supplied state class.
     * @param stateClass The class of the state object to build.
     * @param <T> The type of the state object to build.
     * @return The constructed {@link StateBuilder}.
     */
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

    private StateBuilder(EventTypeBinding typeBinding, Supplier<StateMethodDispatcher<T>> stateMethodDispatcherSupplier) {
        this.typeBinding = typeBinding;
        this.stateMethodDispatcherSupplier = stateMethodDispatcherSupplier;
    }

    /**
     * Fetch events from the supplied {@link EventSource}, and use them to build a state object for the supplied
     * aggregate id.
     * @param eventSource The {@link EventSource} to use to fetch events.
     * @param aggregateId The aggregate id to fetch events for.
     * @return The constructed state object, or {@link Optional}::empty if the aggregate has no state.
     */
    public Optional<T> buildState(EventSource eventSource, UUID aggregateId) {
        return buildState(eventSource, aggregateId, TimeRange.unbounded());
    }

    /**
     * Fetch events within the supplied {@link TimeRange} from the supplied {@link EventSource}, and use them to build
     * a state object for the supplied aggregate id.
     * @param eventSource The {@link EventSource} to use to fetch events.
     * @param aggregateId The aggregate id to fetch events for.
     * @param timeRange The {@link TimeRange} to restricts the replayed events to.
     * @return The constructed state object, or {@link Optional}::empty if the aggregate has no state.
     */
    public Optional<T> buildState(EventSource eventSource, UUID aggregateId, TimeRange timeRange) {
        return createDispatcher().apply(typeBinding.replaying(eventSource, aggregateId, timeRange));
    }

    /**
     * Fetch events from the supplied {@link CachedEventSource}, and use them to build a state object for the supplied
     * aggregate id.
     * @param cachedEventSource The {@link CachedEventSource} to use to fetch events.
     * @param aggregateId The aggregate id to fetch events for.
     * @return The constructed state object, or {@link Optional}::empty if the aggregate has no state.
     */
    public Optional<T> buildState(CachedEventSource cachedEventSource, UUID aggregateId) {
        return buildState(cachedEventSource, aggregateId, TimeRange.unbounded());
    }

    /**
     * Fetch events within the supplied {@link TimeRange} from the supplied {@link CachedEventSource}, and use them to build
     * a state object for the supplied aggregate id.
     * @param cachedEventSource The {@link CachedEventSource} to use to fetch events.
     * @param aggregateId The aggregate id to fetch events for.
     * @param timeRange The {@link TimeRange} to restricts the replayed events to.
     * @return The constructed state object, or {@link Optional}::empty if the aggregate has no state.
     */
    public Optional<T> buildState(CachedEventSource cachedEventSource, UUID aggregateId, TimeRange timeRange) {
        return createDispatcher().apply(typeBinding.replaying(cachedEventSource, aggregateId, timeRange));
    }

    private CachedEventSource preload(EventSource eventSource, Collection<UUID> aggregateIds, TimeRange timeRange) {
        return typeBinding.preload(eventSource, aggregateIds, timeRange);
    }

    /**
     * Fetch events from the supplied {@link EventSource}, and use them to
     * build state objects for the supplied aggregateids.
     * @param eventSource The {@link EventSource} to use to fetch events.
     * @param aggregateIds The aggregate ids to fetch events for.
     * @return The constructed state objects, mapped by aggregate id.
     */
    public Map<UUID, T> buildStates(EventSource eventSource, Collection<UUID> aggregateIds) {
        return buildStates(eventSource, aggregateIds, TimeRange.unbounded());
    }

    /**
     * Fetch events within the supplied {@link TimeRange} from the supplied {@link EventSource}, and use them to
     * build state objects for the supplied aggregateids.
     * @param eventSource The {@link EventSource} to use to fetch events.
     * @param aggregateIds The aggregate ids to fetch events for.
     * @param timeRange The {@link TimeRange} to restricts the replayed events to.
     * @return The constructed state objects, mapped by aggregate id.
     */
    public Map<UUID, T> buildStates(EventSource eventSource, Collection<UUID> aggregateIds, TimeRange timeRange) {
        return buildStates(preload(eventSource, aggregateIds, timeRange), aggregateIds);
    }

    /**
     * Fetch events from the supplied {@link CachedEventSource}, and use them to
     * build state objects for the supplied aggregateids.
     * @param cachedEventSource The {@link CachedEventSource} to use to fetch events.
     * @param aggregateIds The aggregate ids to fetch events for.
     * @return The constructed state objects, mapped by aggregate id.
     */
    public Map<UUID, T> buildStates(CachedEventSource cachedEventSource, Collection<UUID> aggregateIds) {
        return buildStates(cachedEventSource, aggregateIds, TimeRange.unbounded());
    }

    /**
     * Fetch events within the supplied {@link TimeRange} from the supplied {@link CachedEventSource}, and use them to
     * build state objects for the supplied aggregateids.
     * @param cachedEventSource The {@link CachedEventSource} to use to fetch events.
     * @param aggregateIds The aggregate ids to fetch events for.
     * @param timeRange The {@link TimeRange} to restricts the replayed events to.
     * @return The constructed state objects, mapped by aggregate id.
     */
    public Map<UUID, T> buildStates(CachedEventSource cachedEventSource, Collection<UUID> aggregateIds, TimeRange timeRange) {
        return aggregateIds.stream()
                .map(id -> new SimpleEntry<>(id, buildState(cachedEventSource, id)))
                .filter(e -> e.getValue().isPresent())
                .collect(toMap(Entry::getKey, e -> e.getValue().get()));
    }

}
