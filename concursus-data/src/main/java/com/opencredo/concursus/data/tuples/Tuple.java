package com.opencredo.concursus.data.tuples;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * An immutable tuple of named and typed values, conforming to a TupleSchema.
 */
public final class Tuple {

    private final TupleSchema schema;
    private final Object[] values;

    Tuple(TupleSchema schema, Object[] values) {
        this.schema = schema;
        this.values = values;
    }

    /**
     * Get the value contained in the slot with the supplied name.
     * @param name The name of the slot to get the value from.
     * @return The value stored in that slot.
     */
    public Object get(String name) {
        if (name == null) throw new IllegalArgumentException("name must not be null");
        return schema.get(name, values);
    }

    /**
     * Get the value contained in the slot referenced by the supplied key.
     * @param key The key to use to find the slot.
     * @param <T> The type of the value stored in the slot.
     * @return The value stored in the slot.
     */
    public <T> T get(TupleKey<T> key) {
        if (key == null) throw new IllegalArgumentException("key must not be null");
        if (!key.belongsToSchema(schema)) throw new IllegalArgumentException(
                String.format("key %s does not belong to schema %s", key, schema));

        return key.get(values);
    }

    /**
     * Serialise the tuple using the supplied serialiser.
     * @param serialiser The serialiser to use to serialise each value in the tuple.
     * @param <V> The type to serialise each value to, e.g. String.
     * @return A map of serialised values.
     */
    public <V> Map<String, V> serialise(Function<Object, V> serialiser) {
        return schema.serialise(serialiser, values);
    }

    /**
     * Obtain the contents of the tuple as a map.
     * @return The contents of the tuple as a map.
     */
    public Map<String, Object> toMap() {
        return schema.toMap(values);
    }

    /**
     * Get the schema to which the tuple belongs.
     * @return The schema to which the tuple belongs.
     */
    public TupleSchema getSchema() {
        return schema;
    }

    @Override
    public boolean equals(Object o) {
        return this == o
                || (o instanceof Tuple
                && ((Tuple) o).schema.equals(schema)
                && Arrays.deepEquals(((Tuple) o).values, values));
    }

    @Override
    public int hashCode() {
        return Objects.hash(schema, Arrays.deepHashCode(values));
    }

    @Override
    public String toString() {
        return schema.format(values);
    }
}
