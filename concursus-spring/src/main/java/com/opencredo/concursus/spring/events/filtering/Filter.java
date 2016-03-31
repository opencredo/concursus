package com.opencredo.concursus.spring.events.filtering;

import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Filter {
    int value() default 0;
}
