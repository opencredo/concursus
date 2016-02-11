package com.opencredo.concourse.domain.functional;

import java.util.function.Consumer;
import java.util.function.Function;

public final class Consumers {

    private Consumers() {
    }

    public static <I, O> Consumer<I> transform(Consumer<O> c, Function<I, O> f) {
        return i -> c.accept(f.apply(i));
    }
}
