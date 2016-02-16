package com.opencredo.concourse.mapping.methods;

import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.UUID;

public final class DispatchingEventSource<T> {

    private final Class<T> handlerClass;
    private final String aggregateType;
    private final EventSource eventSource;

    DispatchingEventSource(Class<T> handlerClass, String aggregateType, EventSource eventSource) {
        this.handlerClass = handlerClass;
        this.aggregateType = aggregateType;
        this.eventSource = eventSource;
    }

    public DispatchingEventReplayer<T> replaying(UUID aggregateId, TimeRange timeRange) {
        return DispatchingEventReplayer.dispatching(handlerClass, eventSource.replaying(AggregateId.of(aggregateType, aggregateId), timeRange));
    }

    public DispatchingEventReplayer<T> replaying(UUID aggregateId) {
        return replaying(aggregateId, TimeRange.unbounded());
    }
}
