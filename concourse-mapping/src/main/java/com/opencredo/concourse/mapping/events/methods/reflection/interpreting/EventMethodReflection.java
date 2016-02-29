package com.opencredo.concourse.mapping.events.methods.reflection.interpreting;

import com.opencredo.concourse.domain.common.VersionedName;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.mapping.annotations.*;
import com.opencredo.concourse.mapping.events.methods.ordering.CausalOrdering;

import java.lang.reflect.Method;

public final class EventMethodReflection {

    private EventMethodReflection() {
    }

    public static EventType getEventType(String aggregateType, Method method) {
        return EventType.of(aggregateType, getEventName(method));
    }

    public static VersionedName getEventName(Method method) {
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

    public static int getOrdering(Method method) {
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
}
