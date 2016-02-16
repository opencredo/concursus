package com.opencredo.concourse.mapping.methods;

import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.events.preloading.PreloadableEventSource;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public class DispatchingPreloadableEventSource<T> {

    public static <T> DispatchingPreloadableEventSource<T> dispatching(PreloadableEventSource eventSource, Class<T> handlerClass) {
        return new DispatchingPreloadableEventSource<>(
                handlerClass,
                EventInterfaceReflection.getAggregateType(handlerClass),
                EventInterfaceReflection.getEventTypeMatcher(handlerClass),
                eventSource);
    }

    private final Class<T> handlerClass;
    private final String aggregateType;
    private EventTypeMatcher eventTypeMatcher;
    private final PreloadableEventSource eventSource;

    private DispatchingPreloadableEventSource(Class<T> handlerClass,
                                              String aggregateType,
                                              EventTypeMatcher eventTypeMatcher,
                                              PreloadableEventSource eventSource) {
        this.handlerClass = handlerClass;
        this.aggregateType = aggregateType;
        this.eventTypeMatcher = eventTypeMatcher;
        this.eventSource = eventSource;
    }

    public DispatchingEventReplayer<T> replaying(UUID aggregateId, TimeRange timeRange) {
        return DispatchingEventReplayer.dispatching(
                handlerClass,
                eventSource.replaying(
                        eventTypeMatcher,
                        AggregateId.of(aggregateType, aggregateId),
                        timeRange));
    }

    public DispatchingEventReplayer<T> replaying(UUID aggregateId) {
        return replaying(aggregateId, TimeRange.unbounded());
    }

    public DispatchingEventSource<T> preload(Collection<UUID> aggregateIds, TimeRange timeRange) {
        return new DispatchingEventSource<>(
                handlerClass,
                aggregateType,
                eventSource.preload(eventTypeMatcher, aggregateType, aggregateIds, timeRange));
    }

    public DispatchingEventSource<T> preload(Collection<UUID> aggregateIds) {
        return preload(aggregateIds, TimeRange.unbounded());
    }

    public DispatchingEventSource<T> preload(UUID...aggregateIds) {
        return preload(Arrays.asList(aggregateIds));
    }
}
