package com.opencredo.concourse.mapping.events.methods.dispatching;

import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.time.TimeRange;
import com.opencredo.concourse.mapping.events.methods.reflection.EventInterfaceInfo;
import com.opencredo.concourse.mapping.events.methods.reflection.EventTypeBinding;
import com.opencredo.concourse.mapping.events.methods.reflection.MultiEventDispatcher;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class DispatchingEventSource<T> {

    public static <T> DispatchingEventSource<T> dispatching(EventSource eventSource, Class<T> handlerClass) {
        checkNotNull(eventSource, "eventSource must not be null");
        EventInterfaceInfo<T> interfaceInfo = EventInterfaceInfo.forInterface(handlerClass);

        return new DispatchingEventSource<>(interfaceInfo.getEventDispatcher(), interfaceInfo.getEventTypeBinding(), eventSource);
    }

    private final MultiEventDispatcher<T> eventDispatcher;
    private final EventTypeBinding typeBinding;
    private final EventSource eventSource;

    private DispatchingEventSource(MultiEventDispatcher<T> eventDispatcher,
                                   EventTypeBinding typeBinding, EventSource eventSource) {
        this.eventDispatcher = eventDispatcher;
        this.typeBinding = typeBinding;
        this.eventSource = eventSource;
    }

    public DispatchingEventReplayer<T> replaying(UUID aggregateId, TimeRange timeRange) {
        return DispatchingEventReplayer.dispatching(
                eventDispatcher,
                typeBinding.replaying(eventSource, aggregateId, timeRange));
    }

    public DispatchingEventReplayer<T> replaying(UUID aggregateId) {
        return replaying(aggregateId, TimeRange.unbounded());
    }

    public DispatchingCachedEventSource<T> preload(Collection<UUID> aggregateIds, TimeRange timeRange) {
        return DispatchingCachedEventSource.dispatching(
                eventDispatcher,
                typeBinding,
                typeBinding.preload(eventSource, aggregateIds, timeRange));
    }

    public DispatchingCachedEventSource<T> preload(Collection<UUID> aggregateIds) {
        return preload(aggregateIds, TimeRange.unbounded());
    }

    public DispatchingCachedEventSource<T> preload(UUID...aggregateIds) {
        return preload(Arrays.asList(aggregateIds));
    }
}
