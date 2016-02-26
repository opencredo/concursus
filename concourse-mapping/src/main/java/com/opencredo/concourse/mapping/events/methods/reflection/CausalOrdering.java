package com.opencredo.concourse.mapping.events.methods.reflection;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.mapping.annotations.Initial;
import com.opencredo.concourse.mapping.annotations.Ordered;
import com.opencredo.concourse.mapping.annotations.Terminal;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

final class CausalOrdering {

    private static final int INITIAL = Integer.MIN_VALUE;
    private static final int TERMINAL = Integer.MAX_VALUE;
    private static final int PRE_TERMINAL = TERMINAL - 1;

    private CausalOrdering() {
    }

    static Comparator<Event> onMethods(Map<Method, EventType> methodTypeMap) {
        return onEventTypes(methodTypeMap.entrySet().stream()
            .collect(Collectors.toMap(
                    Entry::getValue,
                    e -> getOrdering(e.getKey()))
            ));
    }

    private static int getOrdering(Method method) {
        if (method.isAnnotationPresent(Initial.class)) {
            return INITIAL;
        }

        if (method.isAnnotationPresent(Terminal.class)) {
            return TERMINAL;
        }

        if (method.isAnnotationPresent(Ordered.class)) {
            return method.getAnnotation(Ordered.class).value();
        }

        return PRE_TERMINAL;
    }

    private static Comparator<Event> onEventTypes(Map<EventType, Integer> eventTypeMap) {
        return Comparator.comparing((Event evt) -> eventTypeMap.getOrDefault(EventType.of(evt), PRE_TERMINAL))
                .thenComparing(Event::getEventTimestamp);
    }
}
