package com.opencredo.concourse.domain.events.binding;

import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.events.sourcing.CachedEventSource;
import com.opencredo.concourse.domain.events.sourcing.EventReplayer;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.Collection;
import java.util.UUID;

/**
 * Captures the aggregate type and {@link EventTypeMatcher} for an interface, and uses these to simplify making calls to an {@link EventSource}
 */
public final class EventTypeBinding {

    /**
     * Create an {@link EventTypeBinding} with the supplied aggregate type and {@link EventTypeMatcher}.
     * @param aggregateType The aggregate type to bind.
     * @param eventTypeMatcher The {@link EventTypeMatcher} to bind.
     * @return The constructed {@link EventTypeBinding}.
     */
    public static EventTypeBinding of(String aggregateType, EventTypeMatcher eventTypeMatcher) {
        return new EventTypeBinding(aggregateType, eventTypeMatcher);
    }

    private final String aggregateType;
    private final EventTypeMatcher eventTypeMatcher;

    private EventTypeBinding(String aggregateType, EventTypeMatcher eventTypeMatcher) {
        this.aggregateType = aggregateType;
        this.eventTypeMatcher = eventTypeMatcher;
    }

    private AggregateId addTypeTo(UUID aggregateId) {
        return AggregateId.of(aggregateType, aggregateId);
    }

    /**
     * Using the bound aggregate type and {@link EventTypeMatcher}, preload the requested aggregateIds from the supplied {@link EventSource}
     * @param eventSource The EventSource to preload events from
     * @param aggregateIds The aggregate ids to load events for
     * @param timeRange The time range to query within
     * @return The cached event source
     */
    public CachedEventSource preload(EventSource eventSource, Collection<UUID> aggregateIds, TimeRange timeRange) {
        return eventSource.preload(eventTypeMatcher, aggregateType, aggregateIds, timeRange);
    }

    /**
     * Using the bound  aggregate type and {@link EventTypeMatcher}, replay events for the requested aggregateId from the supplied {@link EventSource}
     * @param eventSource The EventSource to replay events from
     * @param aggregateId The aggregate id to load events for
     * @param timeRange The time range to query within
     * @return The replayer for the aggregate's event history
     */
    public EventReplayer replaying(EventSource eventSource, UUID aggregateId, TimeRange timeRange) {
        return eventSource.replaying(eventTypeMatcher, addTypeTo(aggregateId), timeRange);
    }

    /**
     * Using the bound  aggregate type and {@link EventTypeMatcher}, replay events for the requested aggregateId from the supplied {@link CachedEventSource}
     * @param eventSource The CachedEventSource to replay events from
     * @param aggregateId The aggregate id to load events for
     * @param timeRange The time range to query within
     * @return The replayer for the aggregate's event history
     */
    public EventReplayer replaying(CachedEventSource eventSource, UUID aggregateId, TimeRange timeRange) {
        return eventSource.replaying(addTypeTo(aggregateId), timeRange);
    }
}
