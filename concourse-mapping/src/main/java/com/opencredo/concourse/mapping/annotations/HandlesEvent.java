package com.opencredo.concourse.mapping.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation attached to event-handling methods.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HandlesEvent {
    /**
     * @return The name of the event handled by this method.
     */
    String value() default "";

    /**
     * @return The version of the event handled by this method.
     */
    String version() default "0";
}
