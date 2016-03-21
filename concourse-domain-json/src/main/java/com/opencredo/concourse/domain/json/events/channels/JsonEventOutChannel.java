package com.opencredo.concourse.domain.json.events.channels;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.channels.EventInChannel;
import com.opencredo.concourse.domain.events.channels.EventOutChannel;
import com.opencredo.concourse.domain.json.events.EventJson;

/**
 * A channel through which {@link Event}s can be sent out of the system in JSON serialised form.
 */
public final class JsonEventOutChannel implements EventOutChannel {

    /**
     * Construct an {@link EventOutChannel} which serialises outgoing {@link Event}s to JSON.
     * @param objectMapper The {@link ObjectMapper} to use for serialisation.
     * @param inChannel The {@link EventInChannel} that will receive the serialised {@link Event}s.
     * @return The constructed {@link EventOutChannel}.
     */
    public static JsonEventOutChannel using(ObjectMapper objectMapper, EventInChannel<String> inChannel) {
        return new JsonEventOutChannel(objectMapper, inChannel);
    }

    private final ObjectMapper objectMapper;
    private final EventInChannel<String> inChannel;

    private JsonEventOutChannel(ObjectMapper objectMapper, EventInChannel<String> inChannel) {
        this.objectMapper = objectMapper;
        this.inChannel = inChannel;
    }

    @Override
    public void accept(Event event) {
        inChannel.accept(EventJson.toString(event, objectMapper));
    }
}
