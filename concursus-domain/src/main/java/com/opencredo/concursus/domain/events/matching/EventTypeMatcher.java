package com.opencredo.concursus.domain.events.matching;

import com.opencredo.concursus.data.tuples.TupleSchema;
import com.opencredo.concursus.domain.events.EventType;

import java.util.Map;
import java.util.Optional;

/**
 * Provides parameter type information, in the form of a {@link TupleSchema}, for each {@link EventType} that it knows about.
 */
@FunctionalInterface
public interface EventTypeMatcher {

    /**
     * Create an {@link EventTypeMatcher} that looks up {@link TupleSchema}s in the supplied {@link Map}.
     * @param map The {@link Map} to look up {@link TupleSchema}s in.
     * @return The constructed {@link EventTypeMatcher}.
     */
    static EventTypeMatcher matchingAgainst(Map<EventType, TupleSchema> map) {
        return eventType -> Optional.ofNullable(map.get(eventType));
    }

    /**
     * Return the {@link TupleSchema} matching the supplied {@link EventType}, if known, or {@link Optional}::empty otherwise.
     * @param eventType The {@link EventType} to find a {@link TupleSchema} for.
     * @return The matching {@link TupleSchema}.
     */
    Optional<TupleSchema> match(EventType eventType);

}
