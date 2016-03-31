package com.opencredo.concursus.domain.events.logging;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.channels.EventsOutChannel;
import com.opencredo.concursus.domain.time.TimeUUID;

import java.util.Collection;
import java.util.function.UnaryOperator;

import static java.util.stream.Collectors.toList;

/**
 * Assigns processing ids to {@link Event}s and forwards them to an {@link EventsOutChannel}, returning the collection
 * of events that were logged with processing ids attached.
 */
@FunctionalInterface
public interface EventLog extends UnaryOperator<Collection<Event>> {

    /**
     * Creates an {@link EventLog} that assigns processing ids to {@link Event}s, then dispatches them through an {@link EventsOutChannel}.
     * @param eventsOutChannel The {@link EventsOutChannel} to send {@link Event}s through to be logged.
     * @return The constructed {@link EventLog}
     */
    static EventLog loggingTo(EventsOutChannel eventsOutChannel) {
        return events -> {
            Collection<Event> withProcessingIds = events.stream()
                    .map(event -> event.processed(TimeUUID.timeBased()))
                    .collect(toList());

            eventsOutChannel.accept(withProcessingIds);

            return withProcessingIds;
        };
    }

}
