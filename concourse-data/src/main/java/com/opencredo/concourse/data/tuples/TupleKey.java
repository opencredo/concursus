package com.opencredo.concourse.data.tuples;

/**
 * A key that can be used to retrieve a value directly from a tuple, in a type-safe way.
 * @param <T>
 */
public class TupleKey<T> {

    private final TupleSchema schema;
    private final String name;
    private final Class<T> type;
    private final int index;

    TupleKey(TupleSchema schema, String name, Class<T> type, int index) {
        this.schema = schema;
        this.name = name;
        this.type = type;
        this.index = index;
    }

    T get(Object[] values) {
        return type.cast(values[index]);
    }

    boolean belongsToSchema(TupleSchema schema) {
        return this.schema.equals(schema);
    }

    /**
     * Create a tuple builder that will place the supplied value in the slot referenced by this key.
     * @param value The value to add to the tuple.
     * @return The tuple builder.
     */
    public TupleBuilder of(T value) {
        return map -> map.put(name, value);
    }

    @Override
    public String toString() {
        return "TupleKey<" + name + ">";
    }
}
