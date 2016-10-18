package com.opencredo.concursus.domain.json.events.channels;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.channels.EventInChannel;
import com.opencredo.concursus.domain.events.matching.EventTypeMatcher;
import com.opencredo.concursus.domain.json.events.EventJson;

import java.util.function.Consumer;

/**
 * A channel through which events encoded as JSON can be passed into the system.
 */
public final class JsonEventInChannel {

    private JsonEventInChannel() {
    }

    /**
     * Creates an {@link EventInChannel} through which events encoded as JSON can be passed into the system.
     * @param objectMapper The {@link ObjectMapper} to use to deserialise events.
     * @param typeMatcher The {@link EventTypeMatcher} to use to match
     * {@link com.opencredo.concursus.domain.events.EventType}s to
     * {@link com.opencredo.concursus.data.tuples.TupleSchema}s.
     * @param eventConsumer The {@link Consumer} to pass deserialised {@link Event}s through to.
     * @return The constructed {@link EventInChannel}.
     */
    public static EventInChannel<String> using(ObjectMapper objectMapper, EventTypeMatcher typeMatcher, Consumer<Event> eventConsumer) {
        return JsonRepresentationEventInChannel.using(objectMapper, typeMatcher, eventConsumer)
                .map(eventJson -> EventJson.fromJsonString(eventJson, objectMapper).toRepresentation());
    }
}
