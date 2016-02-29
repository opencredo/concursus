package com.opencredo.concourse.mapping.events.methods.dispatching;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.time.TimeRange;
import com.opencredo.concourse.mapping.events.methods.reflection.EventInterfaceInfo;
import com.opencredo.concourse.domain.events.binding.EventTypeBinding;
import com.opencredo.concourse.mapping.events.methods.reflection.dispatching.MultiTypeEventDispatcher;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class DispatchingEventSource<T> {

    public static <T> DispatchingEventSource<T> dispatching(EventSource eventSource, Class<T> handlerClass) {
        checkNotNull(eventSource, "eventSource must not be null");
        EventInterfaceInfo<T> interfaceInfo = EventInterfaceInfo.forInterface(handlerClass);

        return new DispatchingEventSource<>(
                interfaceInfo.getEventDispatcher(),
                interfaceInfo.getCausalOrderComparator(),
                interfaceInfo.getEventTypeBinding(),
                eventSource);
    }

    private final MultiTypeEventDispatcher<T> eventDispatcher;
    private final Comparator<Event> causalOrderComparator;
    private final EventTypeBinding typeBinding;
    private final EventSource eventSource;

    private DispatchingEventSource(MultiTypeEventDispatcher<T> eventDispatcher,
                                   Comparator<Event> causalOrderComparator, EventTypeBinding typeBinding, EventSource eventSource) {
        this.eventDispatcher = eventDispatcher;
        this.causalOrderComparator = causalOrderComparator;
        this.typeBinding = typeBinding;
        this.eventSource = eventSource;
    }

    public DispatchingEventReplayer<T> replaying(UUID aggregateId, TimeRange timeRange) {
        return DispatchingEventReplayer.dispatching(
                causalOrderComparator,
                eventDispatcher,
                typeBinding.replaying(eventSource, aggregateId, timeRange));
    }

    public DispatchingEventReplayer<T> replaying(UUID aggregateId) {
        return replaying(aggregateId, TimeRange.unbounded());
    }

    public DispatchingCachedEventSource<T> preload(Collection<UUID> aggregateIds, TimeRange timeRange) {
        return DispatchingCachedEventSource.dispatching(
                eventDispatcher,
                causalOrderComparator,
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
