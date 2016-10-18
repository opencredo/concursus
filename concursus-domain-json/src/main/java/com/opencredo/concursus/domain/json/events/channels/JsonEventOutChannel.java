package com.opencredo.concursus.domain.json.events.channels;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.EventMetadata;
import com.opencredo.concursus.domain.events.channels.EventOutChannel;
import com.opencredo.concursus.domain.json.events.EventJson;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A channel through which {@link Event}s can be sent out of the system in JSON serialised form.
 */
public final class JsonEventOutChannel {

    private JsonEventOutChannel() {
    }

    /**
     * Construct an {@link EventOutChannel} which serialises outgoing {@link Event}s to JSON.
     * @param objectMapper The {@link ObjectMapper} to use for serialisation.
     * @param metadataAndJsonConsumer The {@link Consumer} that will receive the {@link EventMetadata} and the JSON-serialised  {@link Event}.
     * @return The constructed {@link EventOutChannel}.
     */
    public static EventOutChannel using(ObjectMapper objectMapper, BiConsumer<EventMetadata, String> metadataAndJsonConsumer) {
        return JsonRepresentationEventOutChannel.using(objectMapper, representation ->
                    metadataAndJsonConsumer.accept(
                            representation.getMetadata(),
                            EventJson.fromRepresentation(representation).toJsonString(objectMapper)));
    }
}
