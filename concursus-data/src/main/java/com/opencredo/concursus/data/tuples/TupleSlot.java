package com.opencredo.concursus.data.tuples;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * A slot in a {@link TupleSchema} which has a name and a type.
 */
public final class TupleSlot {
    /**
     * Create a new {@link List} TupleSlot, with the supplied name and element type.
     * @param name The name of the TupleSlot.
     * @param elementType The element type of the TupleSlot.
     * @return The created TupleSlot.
     */
    public static <T> TupleSlot ofList(String name, Class<T> elementType) {
        if (elementType == null) throw new IllegalArgumentException("elementType must not be null");

        return of(name, Types.listOf(elementType));
    }

    /**
     * Create a new {@link Optional} TupleSlot, with the supplied name and value type.
     * @param name The name of the TupleSlot.
     * @param valueType The value type of the TupleSlot.
     * @return The created TupleSlot.
     */
    public static <T> TupleSlot ofOptional(String name, Class<T> valueType) {
        if (valueType == null) throw new IllegalArgumentException("valueType must not be null");

        return of(name, Types.optionalOf(valueType));
    }

    /**
     * Create a new {@link Map} TupleSlot, with the supplied name and key and value types.
     * @param name The name of the TupleSlot.
     * @param keyType The key type of the TupleSlot.
     * @param valueType The value type of the TupleSlot.
     * @return The created TupleSlot.
     */
    public static <K, V> TupleSlot ofMap(String name, Class<K> keyType, Class<V> valueType) {
        if (keyType == null) throw new IllegalArgumentException("keyType must not be null");
        if (valueType == null) throw new IllegalArgumentException("valueType must not be null");

        return of(name, Types.mapOf(keyType, valueType));
    }

    /**
     * Create a new TupleSlot, with the supplied name and type.
     * @param name The name of the TupleSlot.
     * @param type The type of the TupleSlot.
     * @return The created TupleSlot.
     */
    public static TupleSlot of(String name, Type type) {
        if (name == null) throw new IllegalArgumentException("name must not be null");
        if (type == null) throw new IllegalArgumentException("type must not be null");

        return new TupleSlot(name, type);
    }

    private final String name;
    private final Type type;

    private TupleSlot(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Gets the name of the TupleSlot.
     * @return The name of the TupleSlot.
     */
    public String getName() {
        return name;
    }

    boolean accepts(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Optional) {
            return ((Optional<?>) value).map(this::acceptsOptional).orElse(true);
        }
        return acceptsClass(value.getClass());
    }

    private boolean acceptsOptional(Object presentValue) {
        return Types.isAssignableFrom(type, Optional.class)
                && Types.isAssignableFrom(
                ((ParameterizedType) type).getActualTypeArguments()[0],
                presentValue.getClass());
    }

    boolean acceptsClass(Class<?> klass) {
        return Types.isAssignableFrom(type, klass);
    }

    boolean acceptsType(Type type) {
        return Types.isAssignableFrom(this.type, type);
    }

    <V> Object deserialise(BiFunction<V, Type, Object> deserialiser, Map<String, V> values) {
        return deserialiser.apply(values.get(name), type);
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
        return name + ": " + type.getTypeName();
    }
}
