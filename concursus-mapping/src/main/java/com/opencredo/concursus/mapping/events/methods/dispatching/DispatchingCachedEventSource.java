package com.opencredo.concursus.mapping.events.methods.dispatching;

import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.binding.EventTypeBinding;
import com.opencredo.concursus.domain.events.sourcing.CachedEventSource;
import com.opencredo.concursus.domain.time.TimeRange;
import com.opencredo.concursus.mapping.events.methods.reflection.dispatching.MultiTypeEventDispatcher;

import java.util.Comparator;
import java.util.UUID;

/**
 * Wraps a {@link CachedEventSource}, and dispatches retrieved events to an appropriate handler.
 * @param <T> The type of the handler interface.
 */
public final class DispatchingCachedEventSource<T> {

    static <T> DispatchingCachedEventSource<T> dispatching(MultiTypeEventDispatcher<T> eventDispatcher, Comparator<Event> causalOrderComparator, EventTypeBinding typeBinding, CachedEventSource cachedEventSource) {
        return new DispatchingCachedEventSource<>(eventDispatcher, causalOrderComparator, typeBinding, cachedEventSource);
    }

    private final MultiTypeEventDispatcher<T> eventDispatcher;
    private final Comparator<Event> causalOrderComparator;
    private final EventTypeBinding typeBinding;
    private final CachedEventSource cachedEventSource;

    private DispatchingCachedEventSource(MultiTypeEventDispatcher<T> eventDispatcher, Comparator<Event> causalOrderComparator, EventTypeBinding typeBinding, CachedEventSource cachedEventSource) {
        this.eventDispatcher = eventDispatcher;
        this.causalOrderComparator = causalOrderComparator;
        this.typeBinding = typeBinding;
        this.cachedEventSource = cachedEventSource;
    }

    /**
     * Get a {@link DispatchingEventReplayer} replaying events for the given {@link AggregateId} within the given {@link TimeRange}.
     * @param aggregateId The {@link AggregateId} to replay events for.
     * @param timeRange The {@link TimeRange} to restrict results to.
     * @return A {@link DispatchingEventReplayer} for the retrieved {@link Event}s.
     */
    public DispatchingEventReplayer<T> replaying(UUID aggregateId, TimeRange timeRange) {
        return DispatchingEventReplayer.dispatching(causalOrderComparator, eventDispatcher, typeBinding.replaying(cachedEventSource, aggregateId, timeRange));
    }

    /**
     * Get a {@link DispatchingEventReplayer} replaying all events for the given {@link AggregateId}.
     * @param aggregateId The {@link AggregateId} to replay events for.
     * @return A {@link DispatchingEventReplayer} for the retrieved {@link Event}s.
     */
    public DispatchingEventReplayer<T> replaying(UUID aggregateId) {
        return replaying(aggregateId, TimeRange.unbounded());
    }
}
