package com.opencredo.concursus.domain.events.filtering;

import java.util.Collection;
import java.util.function.UnaryOperator;

public final class Filters {

    private Filters() {
    }

    public static <T> UnaryOperator<T> compose(Collection<? extends UnaryOperator<T>> filters) {
        return t -> filter(filters, t);
    }

    public static <T> T filter(Collection<? extends UnaryOperator<T>> filters, T target) {
        return filters.stream().reduce(
                target,
                (accumulator, filter) -> filter.apply(accumulator),
                (accumulator1, accumulator2) -> {
                    throw new UnsupportedOperationException("Cannot merge filters");
                });
    }
}
