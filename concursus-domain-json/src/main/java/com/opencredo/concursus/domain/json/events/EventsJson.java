package com.opencredo.concursus.domain.json.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.matching.EventTypeMatcher;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Utility class for serialising and deserialising collections of Events to and from JSON.
 */
public final class EventsJson {

    private EventsJson() {
    }

    /**
     * Serialise the supplied collection of {@link Event}s to a single JSON string.
     * @param events The {@link Event}s to serialise.
     * @param objectMapper The {@link ObjectMapper} to use.
     * @return The serialised JSON string.
     */
    public static String toString(Collection<Event> events, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(
                    events.stream()
                        .map(event -> EventJson.fromEvent(event, objectMapper))
                        .collect(toList()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deserialise the supplied JSON array of events to a List of {@link Event}s.
     * @param eventsString The JSON to deserialise.
     * @param eventTypeMatcher The {@link EventTypeMatcher} to use to match
     * {@link com.opencredo.concursus.domain.events.EventType}s to
     * {@link com.opencredo.concursus.data.tuples.TupleSchema}s
     * @param objectMapper The {@link ObjectMapper} to use.
     * @return The deserialised list of {@link Event}s.
     */
    public static List<Event> fromString(String eventsString, EventTypeMatcher eventTypeMatcher, ObjectMapper objectMapper) {
        try {
            EventJson[] eventsJson = objectMapper.readValue(eventsString, objectMapper.getTypeFactory().constructArrayType(EventJson.class));
            return Stream.of(eventsJson)
                    .map(eventJson -> eventJson.toEvent(eventTypeMatcher, objectMapper))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
