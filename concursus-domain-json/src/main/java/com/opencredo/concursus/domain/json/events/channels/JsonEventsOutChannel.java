package com.opencredo.concursus.domain.json.events.channels;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.channels.EventsOutChannel;
import com.opencredo.concursus.domain.json.events.EventsJson;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


/**
 * A channel through which collections of {@link Event}s can be sent out of the system in JSON serialised form.
 */
public final class JsonEventsOutChannel implements EventsOutChannel {

    /**
     * Construct an {@link EventsOutChannel} which serialises outgoing collections of {@link Event}s to JSON.
     * @param objectMapper The {@link ObjectMapper} to use for serialisation.
     * @param eventsAndJsonConsumer The {@link Consumer} that will receive the collections of {@link Event}s and their
     *                              JSON serialisations.
     * @return The constructed {@link EventsOutChannel}.
     */
    public static JsonEventsOutChannel using(ObjectMapper objectMapper,
                                             BiConsumer<Collection<Event>, String> eventsAndJsonConsumer) {
        return new JsonEventsOutChannel(objectMapper, eventsAndJsonConsumer);
    }

    /**
     * Construct an {@link EventsOutChannel} which serialises outgoing collections of {@link Event}s to JSON.
     * @param objectMapper The {@link ObjectMapper} to use for serialisation.
     * @param jsonConsumer The {@link Consumer} that will receive the serialised collections of {@link Event}s.
     * @return The constructed {@link EventsOutChannel}.
     */
    public static JsonEventsOutChannel using(ObjectMapper objectMapper, Consumer<String> jsonConsumer) {
        return new JsonEventsOutChannel(objectMapper, (e, j) -> jsonConsumer.accept(j));
    }

    private final ObjectMapper objectMapper;
    private final BiConsumer<Collection<Event>, String> eventsAndJsonConsumer;

    private JsonEventsOutChannel(ObjectMapper objectMapper, BiConsumer<Collection<Event>, String> eventsAndJsonConsumer) {
        this.objectMapper = objectMapper;
        this.eventsAndJsonConsumer = eventsAndJsonConsumer;
    }

    @Override
    public void accept(Collection<Event> events) {
        eventsAndJsonConsumer.accept(events, EventsJson.toString(events, objectMapper));
    }
}
