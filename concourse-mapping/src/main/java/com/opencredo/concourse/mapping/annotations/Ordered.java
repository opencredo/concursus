package com.opencredo.concourse.mapping.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation applied to event-emitting methods, indicating causal ordering.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Ordered {
    /**
     * @return The causal order of this event.
     */
    int value();
}
