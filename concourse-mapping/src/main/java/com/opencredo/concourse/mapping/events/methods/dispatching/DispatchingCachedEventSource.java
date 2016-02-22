package com.opencredo.concourse.mapping.events.methods.dispatching;

import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.events.sourcing.CachedEventSource;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.UUID;

public final class DispatchingCachedEventSource<T> {

    private final Class<T> handlerClass;
    private final String aggregateType;
    private final CachedEventSource cachedEventSource;

    DispatchingCachedEventSource(Class<T> handlerClass, String aggregateType, CachedEventSource cachedEventSource) {
        this.handlerClass = handlerClass;
        this.aggregateType = aggregateType;
        this.cachedEventSource = cachedEventSource;
    }

    public DispatchingEventReplayer<T> replaying(UUID aggregateId, TimeRange timeRange) {
        return DispatchingEventReplayer.dispatching(handlerClass, cachedEventSource.replaying(AggregateId.of(aggregateType, aggregateId), timeRange));
    }

    public DispatchingEventReplayer<T> replaying(UUID aggregateId) {
        return replaying(aggregateId, TimeRange.unbounded());
    }
}
