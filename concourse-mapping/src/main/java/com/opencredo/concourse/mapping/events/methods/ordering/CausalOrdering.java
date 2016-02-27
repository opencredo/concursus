package com.opencredo.concourse.mapping.events.methods.ordering;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;

import java.util.Comparator;
import java.util.Map;

public final class CausalOrdering {

    public static final int INITIAL = Integer.MIN_VALUE;
    public static final int TERMINAL = Integer.MAX_VALUE;
    public static final int PRE_TERMINAL = TERMINAL - 1;

    private CausalOrdering() {
    }

    public static Comparator<Event> onEventTypes(Map<EventType, Integer> eventTypeMap) {
        return Comparator.comparing((Event evt) -> eventTypeMap.getOrDefault(EventType.of(evt), PRE_TERMINAL))
                .thenComparing(Event::getEventTimestamp);
    }
}
