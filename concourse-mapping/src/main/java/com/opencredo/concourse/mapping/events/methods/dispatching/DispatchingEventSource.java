package com.opencredo.concourse.mapping.events.methods.dispatching;

import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.time.TimeRange;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.events.methods.reflection.EventInterfaceReflection;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class DispatchingEventSource<T> {

    public static <T> DispatchingEventSource<T> dispatching(EventSource eventSource, Class<T> handlerClass) {
        checkNotNull(eventSource, "eventSource must not be null");
        checkNotNull(handlerClass, "handleClass must not be null");
        checkArgument(handlerClass.isAnnotationPresent(HandlesEventsFor.class),
                "Class %s is not annotated with @HandlesEventsFor", handlerClass);

        return new DispatchingEventSource<>(
                handlerClass,
                EventInterfaceReflection.getAggregateType(handlerClass),
                EventInterfaceReflection.getEventTypeMatcher(handlerClass),
                eventSource);
    }

    private final Class<T> handlerClass;
    private final String aggregateType;
    private EventTypeMatcher eventTypeMatcher;
    private final EventSource eventSource;

    private DispatchingEventSource(Class<T> handlerClass,
                                   String aggregateType,
                                   EventTypeMatcher eventTypeMatcher,
                                   EventSource eventSource) {
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

    public DispatchingCachedEventSource<T> preload(Collection<UUID> aggregateIds, TimeRange timeRange) {
        return new DispatchingCachedEventSource<>(
                handlerClass,
                aggregateType,
                eventSource.preload(eventTypeMatcher, aggregateType, aggregateIds, timeRange));
    }

    public DispatchingCachedEventSource<T> preload(Collection<UUID> aggregateIds) {
        return preload(aggregateIds, TimeRange.unbounded());
    }

    public DispatchingCachedEventSource<T> preload(UUID...aggregateIds) {
        return preload(Arrays.asList(aggregateIds));
    }
}
