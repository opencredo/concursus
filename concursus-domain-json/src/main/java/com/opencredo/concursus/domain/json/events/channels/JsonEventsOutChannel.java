package com.opencredo.concursus.domain.json.events.channels;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.channels.EventsInChannel;
import com.opencredo.concursus.domain.events.channels.EventsOutChannel;
import com.opencredo.concursus.domain.json.events.EventsJson;

import java.util.Collection;


/**
 * A channel through which collections of {@link Event}s can be sent out of the system in JSON serialised form.
 */
public final class JsonEventsOutChannel implements EventsOutChannel {

    /**
     * Construct an {@link EventsOutChannel} which serialises outgoing collections of {@link Event}s to JSON.
     * @param objectMapper The {@link ObjectMapper} to use for serialisation.
     * @param inChannel The {@link EventsInChannel} that will receive the serialised collections of {@link Event}s.
     * @return The constructed {@link EventsOutChannel}.
     */
    public static JsonEventsOutChannel using(ObjectMapper objectMapper, EventsInChannel<String> inChannel) {
        return new JsonEventsOutChannel(objectMapper, inChannel);
    }

    private final ObjectMapper objectMapper;
    private final EventsInChannel<String> inChannel;

    private JsonEventsOutChannel(ObjectMapper objectMapper, EventsInChannel<String> inChannel) {
        this.objectMapper = objectMapper;
        this.inChannel = inChannel;
    }

    @Override
    public void accept(Collection<Event> events) {
        inChannel.accept(EventsJson.toString(events, objectMapper));
    }
}
