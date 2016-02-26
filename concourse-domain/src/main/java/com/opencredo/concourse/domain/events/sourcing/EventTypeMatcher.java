package com.opencredo.concourse.domain.events.sourcing;

import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.domain.events.EventType;

import java.util.Map;
import java.util.Optional;

@FunctionalInterface
public interface EventTypeMatcher {

    static EventTypeMatcher matchingAgainst(Map<EventType, TupleSchema> map) {
        return eventType -> Optional.ofNullable(map.get(eventType));
    }

    Optional<TupleSchema> match(EventType eventType);

}
