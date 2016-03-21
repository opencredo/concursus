package com.opencredo.concourse.mapping.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation added to event-emitting interfaces.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HandlesEventsFor {
    /**
     * @return The aggregate type for which this interface emits events.
     */
    String value();
}
