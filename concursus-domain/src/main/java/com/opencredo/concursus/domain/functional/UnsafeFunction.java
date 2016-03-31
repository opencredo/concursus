package com.opencredo.concursus.domain.functional;

import java.util.function.Function;

public interface UnsafeFunction<I, O> {

    static <I, O> Function<I, O> of(UnsafeFunction<I, O> f) {
        return i -> UnsafeSupplier.invoke(() -> f.applyUnsafe(i));
    }

    O applyUnsafe(I input) throws Throwable;
}
