package com.opencredo.concursus.domain.json.events.channels;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.channels.EventOutChannel;
import com.opencredo.concursus.domain.json.events.EventJson;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A channel through which {@link Event}s can be sent out of the system in JSON serialised form.
 */
public final class JsonEventOutChannel implements EventOutChannel {

    /**
     * Construct an {@link EventOutChannel} which serialises outgoing {@link Event}s to JSON.
     * @param objectMapper The {@link ObjectMapper} to use for serialisation.
     * @param eventAndJsonConsumer The {@link Consumer} that will receive the {@link Event} and its JSON serialisation.
     * @return The constructed {@link EventOutChannel}.
     */
    public static JsonEventOutChannel using(ObjectMapper objectMapper, BiConsumer<Event, String> eventAndJsonConsumer) {
        return new JsonEventOutChannel(objectMapper, eventAndJsonConsumer);
    }

    /**
     * Construct an {@link EventOutChannel} which serialises outgoing {@link Event}s to JSON.
     * @param objectMapper The {@link ObjectMapper} to use for serialisation.
     * @param jsonConsumer The {@link Consumer} that will receive the serialised {@link Event}s.
     * @return The constructed {@link EventOutChannel}.
     */
    public static JsonEventOutChannel using(ObjectMapper objectMapper, Consumer<String> jsonConsumer) {
        return new JsonEventOutChannel(objectMapper, (e, j) -> jsonConsumer.accept(j));
    }

    private final ObjectMapper objectMapper;
    private final BiConsumer<Event, String> eventAndJsonConsumer;

    private JsonEventOutChannel(ObjectMapper objectMapper, BiConsumer<Event, String> eventAndJsonConsumer) {
        this.objectMapper = objectMapper;
        this.eventAndJsonConsumer = eventAndJsonConsumer;
    }

    @Override
    public void accept(Event event) {
        eventAndJsonConsumer.accept(event, EventJson.toString(event, objectMapper));
    }
}
