package com.opencredo.concursus.mapping.events.methods.reflection.interpreting;

import com.opencredo.concursus.domain.common.VersionedName;
import com.opencredo.concursus.domain.events.EventCharacteristics;
import com.opencredo.concursus.domain.events.EventType;
import com.opencredo.concursus.mapping.annotations.*;
import com.opencredo.concursus.mapping.events.methods.ordering.CausalOrdering;
import com.opencredo.concursus.mapping.reflection.MethodSelectors;

import java.lang.reflect.Method;

final class EventMethodReflection {

    private EventMethodReflection() {
    }

    static EventType getEventType(String aggregateType, Method method) {
        return EventType.of(aggregateType, getEventName(method));
    }

    static VersionedName getEventName(Method method) {
        if (method.isAnnotationPresent(HandlesEvent.class)) {
            return getEventName(method.getAnnotation(HandlesEvent.class), method.getName());
        }
        if (method.isAnnotationPresent(Name.class)) {
            return getEventName(method.getAnnotation(Name.class));
        }
        return VersionedName.of(method.getName(), "0");
    }

    private static VersionedName getEventName(HandlesEvent handlesEvent, String methodName) {
        return VersionedName.of(
                handlesEvent.value().isEmpty() ? methodName : handlesEvent.value(),
                handlesEvent.version()
        );
    }

    private static VersionedName getEventName(Name name) {
        return VersionedName.of(name.value(), name.version());
    }

    static int getOrdering(Method method) {
        if (MethodSelectors.isFactoryMethod.test(method)) {
            return CausalOrdering.INITIAL;
        }

        if (method.isAnnotationPresent(Initial.class)) {
            return CausalOrdering.INITIAL;
        }

        if (method.isAnnotationPresent(Terminal.class)) {
            return CausalOrdering.TERMINAL;
        }

        if (method.isAnnotationPresent(Ordered.class)) {
            return method.getAnnotation(Ordered.class).value();
        }

        return CausalOrdering.PRE_TERMINAL;
    }

    public static int getCharacteristics(Method method) {
        return method.isAnnotationPresent(Initial.class)
                ? EventCharacteristics.IS_INITIAL
                : method.isAnnotationPresent(Terminal.class)
                    ? EventCharacteristics.IS_TERMINAL
                    : 0;
    }
}
