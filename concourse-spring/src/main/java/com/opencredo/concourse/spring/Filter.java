package com.opencredo.concourse.spring;

import org.springframework.stereotype.Component;

@Component
public @interface Filter {
    int value() default 0;
}
