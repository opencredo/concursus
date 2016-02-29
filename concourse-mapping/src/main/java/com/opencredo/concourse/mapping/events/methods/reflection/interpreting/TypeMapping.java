package com.opencredo.concourse.mapping.events.methods.reflection.interpreting;

import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.domain.events.sourcing.EventTypeMatcher;
import com.opencredo.concourse.mapping.events.methods.ordering.CausalOrdering;

import java.util.Collection;
import java.util.Comparator;

import static java.util.stream.Collectors.toMap;

public interface TypeMapping {

    static EventTypeMatcher makeEventTypeMatcher(Collection<? extends TypeMapping> typeMappings) {
        return EventTypeMatcher.matchingAgainst(typeMappings.stream().collect(toMap(TypeMapping::getEventType, TypeMapping::getTupleSchema)));
    }

    static Comparator<Event> makeCausalOrdering(Collection<? extends TypeMapping> typeMappings) {
        return CausalOrdering.onEventTypes(typeMappings.stream().collect(toMap(TypeMapping::getEventType, TypeMapping::getCausalOrder)));
    }

    EventType getEventType();
    TupleSchema getTupleSchema();
    int getCausalOrder();

}
