package com.opencredo.concourse.domain.events.channels;

import com.opencredo.concourse.domain.events.Event;

import java.util.function.Consumer;

/**
 * A channel through which {@link Event}s are sent out of the event system.
 */
@FunctionalInterface
public interface EventOutChannel extends Consumer<Event> {

    /**
     * Send an {@link Event} out of the system through this channel.
     * @param event The {@link Event} to dispatch.
     */
    @Override
    void accept(Event event);

    /**
     * Convert into an {@link EventsOutChannel} that handles batches of events (by passing each @{link Event}
     * in the batch into this {@link EventOutChannel}, one at a time).
     * @return The constructed {@link EventsOutChannel}.
     */
    default EventsOutChannel toEventsOutChannel() {
        return events -> events.forEach(this::accept);
    }

}
