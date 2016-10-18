package com.opencredo.concursus.domain.json.events.channels;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.EventRepresentation;
import com.opencredo.concursus.domain.events.channels.EventInChannel;
import com.opencredo.concursus.domain.events.matching.EventTypeMatcher;
import com.opencredo.concursus.domain.json.events.EventJson;

import java.util.Map;
import java.util.function.Consumer;

/**
 * A channel through which events encoded as JSON can be passed into the system.
 */
public final class JsonRepresentationEventInChannel implements EventInChannel<EventRepresentation<Map<String, JsonNode>>> {

    /**
     * Creates an {@link EventInChannel} through which events encoded as JSON can be passed into the system.
     * @param objectMapper The {@link ObjectMapper} to use to deserialise events.
     * @param typeMatcher The {@link EventTypeMatcher} to use to match
     * {@link com.opencredo.concursus.domain.events.EventType}s to
     * {@link com.opencredo.concursus.data.tuples.TupleSchema}s.
     * @param eventConsumer The {@link Consumer} to pass deserialised {@link Event}s through to.
     * @return The constructed {@link EventInChannel}.
     */
    public static JsonRepresentationEventInChannel using(ObjectMapper objectMapper, EventTypeMatcher typeMatcher, Consumer<Event> eventConsumer) {
        return new JsonRepresentationEventInChannel(objectMapper, typeMatcher, eventConsumer);
    }

    private final ObjectMapper objectMapper;
    private final EventTypeMatcher typeMatcher;
    private final Consumer<Event> outChannel;

    private JsonRepresentationEventInChannel(ObjectMapper objectMapper, EventTypeMatcher typeMatcher, Consumer<Event> outChannel) {
        this.objectMapper = objectMapper;
        this.typeMatcher = typeMatcher;
        this.outChannel = outChannel;
    }

    @Override
    public void accept(EventRepresentation<Map<String, JsonNode>> input) {
        outChannel.accept(EventJson.fromRepresentation(input)
                .toEvent(typeMatcher, objectMapper)
                .orElseThrow(() -> new IllegalArgumentException("No mapping found for event type " + input.getType())));
    }
}
