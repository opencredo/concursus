package com.opencredo.concursus.domain.events.channels;

import com.opencredo.concursus.domain.events.Event;

import java.util.function.Consumer;
import java.util.function.Predicate;

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

    /**
     * Filter this channel with the supplied {@link Predicate}.
     * @param eventFilter The {@link Predicate} to use to filter this channel.
     * @return The filtered {@link EventOutChannel}.
     */
    default EventOutChannel filter(Predicate<Event> eventFilter) {
        return event -> {
            if (eventFilter.test(event)) {
                accept(event);
            }
        };
    }

}
