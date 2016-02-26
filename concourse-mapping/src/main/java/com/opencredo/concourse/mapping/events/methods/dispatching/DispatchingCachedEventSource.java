package com.opencredo.concourse.mapping.events.methods.dispatching;

import com.opencredo.concourse.domain.events.sourcing.CachedEventSource;
import com.opencredo.concourse.domain.time.TimeRange;
import com.opencredo.concourse.mapping.events.methods.reflection.EventTypeBinding;
import com.opencredo.concourse.mapping.events.methods.reflection.MultiEventDispatcher;

import java.util.UUID;

public final class DispatchingCachedEventSource<T> {

    static <T> DispatchingCachedEventSource<T> dispatching(MultiEventDispatcher<T> eventDispatcher, EventTypeBinding typeBinding, CachedEventSource cachedEventSource) {
        return new DispatchingCachedEventSource<>(eventDispatcher, typeBinding, cachedEventSource);
    }

    private final MultiEventDispatcher<T> eventDispatcher;
    private final EventTypeBinding typeBinding;
    private final CachedEventSource cachedEventSource;

    private DispatchingCachedEventSource(MultiEventDispatcher<T> eventDispatcher, EventTypeBinding typeBinding, CachedEventSource cachedEventSource) {
        this.eventDispatcher = eventDispatcher;
        this.typeBinding = typeBinding;
        this.cachedEventSource = cachedEventSource;
    }

    public DispatchingEventReplayer<T> replaying(UUID aggregateId, TimeRange timeRange) {
        return DispatchingEventReplayer.dispatching(eventDispatcher, typeBinding.replaying(cachedEventSource, aggregateId, timeRange));
    }

    public DispatchingEventReplayer<T> replaying(UUID aggregateId) {
        return replaying(aggregateId, TimeRange.unbounded());
    }
}
