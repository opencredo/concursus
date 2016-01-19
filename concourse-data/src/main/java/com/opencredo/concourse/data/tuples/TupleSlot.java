package com.opencredo.concourse.data.tuples;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A slot in a TupleSchema which has a name and a type.
 */
public final class TupleSlot {

    /**
     * Create a new TupleSlot, with the supplied name and type.
     * @param name The name of the TupleSlot.
     * @param type The type of the TupleSlot.
     * @return The created TupleSlot.
     */
    public static TupleSlot of(String name, Type type) {
        checkNotNull(name, "name must not be null");
        checkNotNull(type, "type must not be null");

        return new TupleSlot(name, TypeToken.of(type));
    }

    private final String name;
    private final TypeToken<?> type;

    private TupleSlot(String name, TypeToken<?> type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Gets the name of the TupleSlot.
     * @return The name of the TupleSlot.
     */
    String getName() {
        return name;
    }

    boolean accepts(Object value) {
        return acceptsType(value.getClass());
    }

    boolean acceptsType(Class<?> klass) {
        return type.getRawType().isAssignableFrom(klass);
    }

    <V> Object deserialise(BiFunction<V, Type, Object> deserialiser, Map<String, V> values) {
        return deserialiser.apply(values.get(name), type.getType());
    }

    @Override
    public boolean equals(Object o) {
        return this == o
                || (o instanceof TupleSlot
                && ((TupleSlot) o).name.equals(name)
                && ((TupleSlot) o).type.equals(type));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return name + ": " + type;
    }
}
