package com.opencredo.concursus.domain.functional;

public interface UnsafeSupplier<T> {

    static <T, E extends Throwable> T invoke(UnsafeSupplier<T> f) throws E {
        try {
            return f.get();
        } catch (Throwable e) {
            throw (E) e;
        }
    }
    T get() throws Throwable;
}
