package com.opencredo.concursus.domain.json.events.channels;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.EventRepresentation;
import com.opencredo.concursus.domain.events.channels.EventOutChannel;
import com.opencredo.concursus.domain.json.events.EventJson;

import java.util.Map;
import java.util.function.Consumer;

/**
 * A channel through which {@link Event}s can be sent out of the system as JSON {@link com.opencredo.concursus.domain.events.EventRepresentation}s.
 */
public final class JsonRepresentationEventOutChannel implements EventOutChannel {

    /**
     * Construct an {@link EventOutChannel} which serialises outgoing {@link Event}s to JSON representations.
     * @param objectMapper The {@link ObjectMapper} to use for serialisation.
     * @param jsonRepresentationConsumer The {@link Consumer} that will receive the represented {@link Event}s.
     * @return The constructed {@link EventOutChannel}.
     */
    public static JsonRepresentationEventOutChannel using(
            ObjectMapper objectMapper, Consumer<EventRepresentation<Map<String, JsonNode>>> jsonRepresentationConsumer) {
        return new JsonRepresentationEventOutChannel(objectMapper, jsonRepresentationConsumer);
    }

    private final ObjectMapper objectMapper;
    private final Consumer<EventRepresentation<Map<String, JsonNode>>> jsonRepresentationConsumer;

    private JsonRepresentationEventOutChannel(ObjectMapper objectMapper, Consumer<EventRepresentation<Map<String, JsonNode>>> jsonRepresentationConsumer) {
        this.objectMapper = objectMapper;
        this.jsonRepresentationConsumer = jsonRepresentationConsumer;
    }

    @Override
    public void accept(Event event) {
        jsonRepresentationConsumer.accept(EventJson.fromEvent(event, objectMapper).toRepresentation());
    }
}
