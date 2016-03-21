package com.opencredo.concourse.domain.json.events.channels;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.channels.EventInChannel;
import com.opencredo.concourse.domain.events.channels.EventOutChannel;
import com.opencredo.concourse.domain.events.matching.EventTypeMatcher;
import com.opencredo.concourse.domain.json.events.EventJson;

/**
 * A channel through which events encoded as JSON can be passed into the system.
 */
public final class JsonEventInChannel implements EventInChannel<String> {

    /**
     * Creates an {@link EventInChannel} through which events encoded as JSON can be passed into the system.
     * @param objectMapper The {@link ObjectMapper} to use to deserialise events.
     * @param typeMatcher The {@link EventTypeMatcher} to use to match
     * {@link com.opencredo.concourse.domain.events.EventType}s to
     * {@link com.opencredo.concourse.data.tuples.TupleSchema}s.
     * @param outChannel The {@link EventOutChannel} to pass deserialised {@link Event}s through to.
     * @return The constructed {@link EventInChannel}.
     */
    public static JsonEventInChannel using(ObjectMapper objectMapper, EventTypeMatcher typeMatcher, EventOutChannel outChannel) {
        return new JsonEventInChannel(objectMapper, typeMatcher, outChannel);
    }

    private final ObjectMapper objectMapper;
    private final EventTypeMatcher typeMatcher;
    private final EventOutChannel outChannel;

    private JsonEventInChannel(ObjectMapper objectMapper, EventTypeMatcher typeMatcher, EventOutChannel outChannel) {
        this.objectMapper = objectMapper;
        this.typeMatcher = typeMatcher;
        this.outChannel = outChannel;
    }

    @Override
    public void accept(String input) {
        EventJson.fromString(input, typeMatcher, objectMapper).ifPresent(outChannel::accept);
    }
}
