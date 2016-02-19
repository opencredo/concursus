package com.opencredo.concourse.mapping.events.methods.dispatching;

import com.opencredo.concourse.domain.events.sourcing.EventSource;

public final class DispatchingEventSourceFactory {

    public static DispatchingEventSourceFactory dispatching(EventSource eventSource) {
        return new DispatchingEventSourceFactory(eventSource);
    }

    private final EventSource eventSource;

    private DispatchingEventSourceFactory(EventSource eventSource) {
        this.eventSource = eventSource;
    }

    public <T> DispatchingEventSource<T> to(Class<T> handlerClass) {
        return DispatchingEventSource.dispatching(eventSource, handlerClass);
    }

}
