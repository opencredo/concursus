package com.opencredo.concourse.mapping.methods;

import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.events.sourcing.PreloadedEventSource;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.UUID;

public final class DispatchingPreloadedEventSource<T> {

    private final Class<T> handlerClass;
    private final String aggregateType;
    private final PreloadedEventSource preloadedEventSource;

    DispatchingPreloadedEventSource(Class<T> handlerClass, String aggregateType, PreloadedEventSource preloadedEventSource) {
        this.handlerClass = handlerClass;
        this.aggregateType = aggregateType;
        this.preloadedEventSource = preloadedEventSource;
    }

    public DispatchingEventReplayer<T> replaying(UUID aggregateId, TimeRange timeRange) {
        return DispatchingEventReplayer.dispatching(handlerClass, preloadedEventSource.replaying(AggregateId.of(aggregateType, aggregateId), timeRange));
    }

    public DispatchingEventReplayer<T> replaying(UUID aggregateId) {
        return replaying(aggregateId, TimeRange.unbounded());
    }
}
