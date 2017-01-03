package com.opencredo.concursus.data.tuples;

import java.util.Objects;

/**
 * A key that can be used to retrieve a value directly from a tuple, in a type-safe way.
 * @param <T>
 */
public class TupleKey<T> {

    private final TupleSchema schema;
    private final String name;
    private final int index;

    TupleKey(TupleSchema schema, String name, int index) {
        this.schema = schema;
        this.name = name;
        this.index = index;
    }

    @SuppressWarnings("unchecked")
    T get(Object[] values) {
        return (T) values[index];
    }

    void set(Object[] values, Object value) {
        values[index] = value;
    }

    boolean belongsToSchema(TupleSchema schema) {
        return this.schema.equals(schema);
    }

    /**
     * Create a tuple builder that will place the supplied value in the slot referenced by this key.
     * @param value The value to add to the tuple.
     * @return The tuple builder.
     */
    public TupleKeyValue of(T value) {
        if (value == null) throw new IllegalArgumentException("value must not be null");
        return new TupleKeyValue(this, value);
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return this == o
                || (o instanceof TupleKey
                    && ((TupleKey<?>) o).schema.equals(schema)
                    && ((TupleKey<?>) o).index == index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schema, index);
    }

    @Override
    public String toString() {
        return "TupleKey<" + name + ">";
    }
}
