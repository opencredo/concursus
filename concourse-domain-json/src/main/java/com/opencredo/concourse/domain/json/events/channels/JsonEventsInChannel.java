package com.opencredo.concourse.domain.json.events.channels;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.channels.EventsInChannel;
import com.opencredo.concourse.domain.events.channels.EventsOutChannel;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.domain.json.events.EventsJson;

/**
 * A channel through which collections of events encoded as JSON can be passed into the system.
 */
public final class JsonEventsInChannel implements EventsInChannel<String> {

    /**
     * Creates an {@link EventsInChannel} through which collections of events encoded as JSON can be passed into the system.
     * @param objectMapper The {@link ObjectMapper} to use to deserialise events.
     * @param typeMatcher The {@link EventTypeMatcher} to use to match
     * {@link com.opencredo.concourse.domain.events.EventType}s to
     * {@link com.opencredo.concourse.data.tuples.TupleSchema}s.
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
