package com.opencredo.concourse.mapping.events.methods.dispatching;

import com.opencredo.concourse.domain.events.sourcing.EventSource;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Binds an {@link EventSource} to create {@link DispatchingEventSource}s for different handler classes.
 */
public final class DispatchingEventSourceFactory {

    /**
     * Bind the supplied {@link EventSource} and return a factory that creates {@link DispatchingEventSource}s for
     * different handler classes.
     * @param eventSource The {@link EventSource} to bind.
     * @return The constructed {@link DispatchingEventSourceFactory}.
     */
    public static DispatchingEventSourceFactory dispatching(EventSource eventSource) {
        checkNotNull(eventSource, "eventSource must not be null");

        return new DispatchingEventSourceFactory(eventSource);
    }

    private final EventSource eventSource;

    private DispatchingEventSourceFactory(EventSource eventSource) {
        this.eventSource = eventSource;
    }

    /**
     * Create a {@link DispatchingEventSource} for the supplied handler class.
     * @param handlerClass The handler class to create a {@link DispatchingEventSource} for.
     * @param <T> The type of the handler class.
     * @return The constructed {@link DispatchingEventSource}.
     */
    public <T> DispatchingEventSource<T> dispatchingTo(Class<T> handlerClass) {
        return DispatchingEventSource.dispatching(eventSource, handlerClass);
    }

}
