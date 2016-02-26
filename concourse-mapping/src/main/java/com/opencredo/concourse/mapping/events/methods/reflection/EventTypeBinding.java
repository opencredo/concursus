package com.opencredo.concourse.mapping.events.methods.reflection;

import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.events.sourcing.CachedEventSource;
import com.opencredo.concourse.domain.events.sourcing.EventReplayer;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.Collection;
import java.util.UUID;

public final class EventTypeBinding {

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

    public CachedEventSource preload(EventSource eventSource, Collection<UUID> aggregateIds, TimeRange timeRange) {
        return eventSource.preload(eventTypeMatcher, aggregateType, aggregateIds, timeRange);
    }

    public EventReplayer replaying(EventSource eventSource, UUID aggregateId, TimeRange timeRange) {
        return eventSource.replaying(eventTypeMatcher, addTypeTo(aggregateId), timeRange);
    }

    public EventReplayer replaying(CachedEventSource eventSource, UUID aggregateId, TimeRange timeRange) {
        return eventSource.replaying(addTypeTo(aggregateId), timeRange);
    }
}
