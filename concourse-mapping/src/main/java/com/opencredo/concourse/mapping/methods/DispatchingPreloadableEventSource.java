package com.opencredo.concourse.mapping.methods;

import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.events.preloading.PreloadableEventSource;
import com.opencredo.concourse.domain.events.preloading.TypeMatchedPreloadableEventSource;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.Collection;
import java.util.UUID;

public class DispatchingPreloadableEventSource<T> {

    public static <T> DispatchingPreloadableEventSource<T> dispatching(PreloadableEventSource eventSource, Class<T> handlerClass) {
        return new DispatchingPreloadableEventSource<>(
                handlerClass,
                EventInterfaceReflection.getAggregateType(handlerClass),
                eventSource.matchingWith(EventInterfaceReflection.getEventTypeMatcher(handlerClass)));
    }

    private final Class<T> handlerClass;
    private final String aggregateType;
    private final TypeMatchedPreloadableEventSource eventSource;

    private DispatchingPreloadableEventSource(Class<T> handlerClass,
                                              String aggregateType, TypeMatchedPreloadableEventSource eventSource) {
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

    public DispatchingEventSource<T> preload(Collection<UUID> aggregateIds, TimeRange timeRange) {
        return new DispatchingEventSource<>(
                handlerClass,
                aggregateType,
                eventSource.preload(aggregateType, aggregateIds, timeRange));
    }
}
