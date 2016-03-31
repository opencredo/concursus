package com.opencredo.concursus.domain.json.events.channels;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.channels.EventsInChannel;
import com.opencredo.concursus.domain.events.channels.EventsOutChannel;
import com.opencredo.concursus.domain.events.matching.EventTypeMatcher;
import com.opencredo.concursus.domain.json.events.EventsJson;

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
     * @param outChannel The {@link EventsOutChannel} to pass collections of deserialised {@link Event}s through to.
     * @return The constructed {@link EventsInChannel}.
     */
    public static JsonEventsInChannel using(ObjectMapper objectMapper, EventTypeMatcher typeMatcher, EventsOutChannel outChannel) {
        return new JsonEventsInChannel(objectMapper, typeMatcher, outChannel);
    }

    private final ObjectMapper objectMapper;
    private final EventTypeMatcher typeMatcher;
    private final EventsOutChannel outChannel;

    private JsonEventsInChannel(ObjectMapper objectMapper, EventTypeMatcher typeMatcher, EventsOutChannel outChannel) {
        this.objectMapper = objectMapper;
        this.typeMatcher = typeMatcher;
        this.outChannel = outChannel;
    }

    @Override
    public void accept(String input) {
        outChannel.accept(EventsJson.fromString(input, typeMatcher, objectMapper));
    }
}
