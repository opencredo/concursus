package com.opencredo.concursus.domain.json.events.channels;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.channels.EventsInChannel;
import com.opencredo.concursus.domain.events.matching.EventTypeMatcher;
import com.opencredo.concursus.domain.json.events.EventsJson;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * A channel through which collections of events encoded as JSON can be passed into the system.
 */
public final class JsonEventsInChannel implements EventsInChannel<String> {

    /**
     * Creates an {@link EventsInChannel} through which collections of events encoded as JSON can be passed into the system.
     * @param objectMapper The {@link ObjectMapper} to use to deserialise events.
     * @param typeMatcher The {@link EventTypeMatcher} to use to match
     * {@link com.opencredo.concursus.domain.events.EventType}s to
     * {@link com.opencredo.concursus.data.tuples.TupleSchema}s.
     * @param eventsConsumer The {@link Consumer} to pass collections of deserialised {@link Event}s through to.
     * @return The constructed {@link EventsInChannel}.
     */
    public static JsonEventsInChannel using(ObjectMapper objectMapper, EventTypeMatcher typeMatcher, Consumer<Collection<Event>> eventsConsumer) {
        return new JsonEventsInChannel(objectMapper, typeMatcher, eventsConsumer);
    }

    private final ObjectMapper objectMapper;
    private final EventTypeMatcher typeMatcher;
    private final Consumer<Collection<Event>> eventsConsumer;

    private JsonEventsInChannel(ObjectMapper objectMapper, EventTypeMatcher typeMatcher, Consumer<Collection<Event>> eventsConsumer) {
        this.objectMapper = objectMapper;
        this.typeMatcher = typeMatcher;
        this.eventsConsumer = eventsConsumer;
    }

    @Override
    public void accept(String input) {
        eventsConsumer.accept(EventsJson.fromString(input, typeMatcher, objectMapper));
    }
}
