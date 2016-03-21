package com.opencredo.concourse.domain.json.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.matching.EventTypeMatcher;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public final class EventsJson {

    public static String toString(Collection<Event> events, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(
                    events.stream()
                        .map(event -> EventJson.of(event, objectMapper))
                        .collect(toList()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

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
