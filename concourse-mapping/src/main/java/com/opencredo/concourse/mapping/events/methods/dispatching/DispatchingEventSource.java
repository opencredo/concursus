package com.opencredo.concourse.mapping.events.methods.dispatching;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.time.TimeRange;
import com.opencredo.concourse.mapping.events.methods.reflection.EmitterInterfaceInfo;
import com.opencredo.concourse.domain.events.binding.EventTypeBinding;
import com.opencredo.concourse.mapping.events.methods.reflection.dispatching.MultiTypeEventDispatcher;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Wraps an {@link EventSource} and dispatches replayed events to a suitable handler class.
 * @param <T> The type of the handler class.
 */
public class DispatchingEventSource<T> {

    /**
     * Create a {@link DispatchingEventSource} that dispatches events to the supplied handler class;
     * @param eventSource The {@link EventSource} to replay events from.
     * @param handlerClass The handler class to dispatch events to.
     * @param <T> The type of the handler class.
     * @return The constructed {@link DispatchingEventSource}.
     */
    public static <T> DispatchingEventSource<T> dispatching(EventSource eventSource, Class<T> handlerClass) {
        checkNotNull(eventSource, "eventSource must not be null");
        EmitterInterfaceInfo<T> interfaceInfo = EmitterInterfaceInfo.forInterface(handlerClass);

        return new DispatchingEventSource<>(
                interfaceInfo.getEventDispatcher(),
                interfaceInfo.getCausalOrderComparator(),
                interfaceInfo.getEventTypeBinding(),
                eventSource);
    }

    private final MultiTypeEventDispatcher<T> eventDispatcher;
    private final Comparator<Event> causalOrderComparator;
    private final EventTypeBinding typeBinding;
    private final EventSource eventSource;

    private DispatchingEventSource(MultiTypeEventDispatcher<T> eventDispatcher,
                                   Comparator<Event> causalOrderComparator,
                                   EventTypeBinding typeBinding,
                                   EventSource eventSource) {
        this.eventDispatcher = eventDispatcher;
        this.causalOrderComparator = causalOrderComparator;
        this.typeBinding = typeBinding;
        this.eventSource = eventSource;
    }

    /**
     * Get a {@link DispatchingEventReplayer} replaying events for the specified aggregate id within the specified
     * {@link TimeRange}.
     * @param aggregateId The aggregate id to replay events for.
     * @param timeRange The {@link TimeRange} to restrict events to.
     * @return The constructed {@link DispatchingEventReplayer}.
     */
    public DispatchingEventReplayer<T> replaying(UUID aggregateId, TimeRange timeRange) {
        return DispatchingEventReplayer.dispatching(
                causalOrderComparator,
                eventDispatcher,
                typeBinding.replaying(eventSource, aggregateId, timeRange));
    }

    /**
     * Get a {@link DispatchingEventReplayer} replaying all events for the specified aggregate id.
     * @param aggregateId The aggregate id to replay events for.
     * @return The constructed {@link DispatchingEventReplayer}.
     */
    public DispatchingEventReplayer<T> replaying(UUID aggregateId) {
        return replaying(aggregateId, TimeRange.unbounded());
    }

    /**
     * Preload events for the supplied aggregate ids within the specified {@link TimeRange}.
     * @param aggregateIds The aggregate ids to preload events for.
     * @param timeRange The {@link TimeRange} to restrict the preloaded events to.
     * @return A constructed {@link DispatchingCachedEventSource} caching the preloaded events.
     */
    public DispatchingCachedEventSource<T> preload(Collection<UUID> aggregateIds, TimeRange timeRange) {
        return DispatchingCachedEventSource.dispatching(
                eventDispatcher,
                causalOrderComparator,
                typeBinding,
                typeBinding.preload(eventSource, aggregateIds, timeRange));
    }

    /**
     * Preload all events for the supplied aggregate ids.
     * @param aggregateIds The aggregate ids to preload events for.
     * @return A constructed {@link DispatchingCachedEventSource} caching the preloaded events.
     */
    public DispatchingCachedEventSource<T> preload(Collection<UUID> aggregateIds) {
        return preload(aggregateIds, TimeRange.unbounded());
    }

    /**
     * Preload all events for the supplied aggregate ids.
     * @param aggregateIds The aggregate ids to preload events for.
     * @return A constructed {@link DispatchingCachedEventSource} caching the preloaded events.
     */
    public DispatchingCachedEventSource<T> preload(UUID...aggregateIds) {
        return preload(Arrays.asList(aggregateIds));
    }
}
