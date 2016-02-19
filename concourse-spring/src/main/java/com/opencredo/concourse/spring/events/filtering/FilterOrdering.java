package com.opencredo.concourse.spring.events.filtering;

import java.util.Comparator;

public final class FilterOrdering {

    private FilterOrdering() {
    }

    public static final Comparator<Object> filterOrderComparator = Comparator.comparing(o ->
            o.getClass().isAnnotationPresent(Filter.class)
                    ? o.getClass().getAnnotation(Filter.class).value()
                    : Integer.MAX_VALUE);
}
