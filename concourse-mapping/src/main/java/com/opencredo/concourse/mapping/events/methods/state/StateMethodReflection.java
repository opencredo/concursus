package com.opencredo.concourse.mapping.events.methods.state;

import com.opencredo.concourse.domain.common.VersionedName;
import com.opencredo.concourse.mapping.annotations.HandlesEvent;

import java.lang.reflect.Method;

public class StateMethodReflection {

    public static VersionedName getName(Method method) {
        HandlesEvent annotation = method.getAnnotation(HandlesEvent.class);
        return VersionedName.of(
                annotation.value().isEmpty() ? method.getName() : annotation.value(),
                annotation.version().isEmpty() ? "0" : annotation.version());
    }
}
