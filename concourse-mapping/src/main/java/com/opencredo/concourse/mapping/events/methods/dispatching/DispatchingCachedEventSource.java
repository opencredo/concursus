package com.opencredo.concourse.mapping.events.methods.dispatching;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.sourcing.CachedEventSource;
import com.opencredo.concourse.domain.time.TimeRange;
import com.opencredo.concourse.domain.events.binding.EventTypeBinding;
import com.opencredo.concourse.mapping.events.methods.reflection.dispatching.MultiTypeEventDispatcher;

import java.util.Comparator;
import java.util.UUID;

public final class DispatchingCachedEventSource<T> {

    static <T> DispatchingCachedEventSource<T> dispatching(MultiTypeEventDispatcher<T> eventDispatcher, Comparator<Event> causalOrderComparator, EventTypeBinding typeBinding, CachedEventSource cachedEventSource) {
        return new DispatchingCachedEventSource<>(eventDispatcher, causalOrderComparator, typeBinding, cachedEventSource);
    }

    private final MultiTypeEventDispatcher<T> eventDispatcher;
    private final Comparator<Event> causalOrderComparator;
    private final EventTypeBinding typeBinding;
    private final CachedEventSource cachedEventSource;

    private DispatchingCachedEventSource(MultiTypeEventDispatcher<T> eventDispatcher, Comparator<Event> causalOrderComparator, EventTypeBinding typeBinding, CachedEventSource cachedEventSource) {
        this.eventDispatcher = eventDispatcher;
        this.causalOrderComparator = causalOrderComparator;
        this.typeBinding = typeBinding;
        this.cachedEventSource = cachedEventSource;
    }

    public DispatchingEventReplayer<T> replaying(UUID aggregateId, TimeRange timeRange) {
        return DispatchingEventReplayer.dispatching(causalOrderComparator, eventDispatcher, typeBinding.replaying(cachedEventSource, aggregateId, timeRange));
    }

    public DispatchingEventReplayer<T> replaying(UUID aggregateId) {
        return replaying(aggregateId, TimeRange.unbounded());
    }
}
