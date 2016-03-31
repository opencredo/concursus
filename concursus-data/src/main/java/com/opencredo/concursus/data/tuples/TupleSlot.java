package com.opencredo.concursus.data.tuples;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkNotNull;

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
        checkNotNull(elementType, "elementType must not be null");

        return of(name, Types.listOf(elementType).getType());
    }

    /**
     * Create a new {@link Optional} TupleSlot, with the supplied name and value type.
     * @param name The name of the TupleSlot.
     * @param valueType The value type of the TupleSlot.
     * @return The created TupleSlot.
     */
    public static <T> TupleSlot ofOptional(String name, Class<T> valueType) {
        checkNotNull(valueType, "valueType must not be null");

        return of(name, Types.optionalOf(valueType).getType());
    }

    /**
     * Create a new {@link Map} TupleSlot, with the supplied name and key and value types.
     * @param name The name of the TupleSlot.
     * @param keyType The key type of the TupleSlot.
     * @param valueType The value type of the TupleSlot.
     * @return The created TupleSlot.
     */
    public static <K, V> TupleSlot ofMap(String name, Class<K> keyType, Class<V> valueType) {
        checkNotNull(keyType, "keyType must not be null");
        checkNotNull(valueType, "valueType must not be null");

        return of(name, Types.mapOf(keyType, valueType).getType());
    }

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
        return type.getRawType().isAssignableFrom(Optional.class)
                && TypeToken.of(((ParameterizedType) type.getType()).getActualTypeArguments()[0])
                .getRawType().isAssignableFrom(presentValue.getClass());
    }

    boolean acceptsClass(Class<?> klass) {
        final Class<?> rawType = type.getRawType();
        return rawType.isAssignableFrom(klass)
                || rawType.isPrimitive() && type.isAssignableFrom(unboxed(klass));
    }

    boolean acceptsType(Type type) {
        return this.type.isAssignableFrom(type)
                || this.type.getRawType().isPrimitive() && this.type.isAssignableFrom(unboxed(type));
    }

    private static final Map<Type, Type> unboxed = ImmutableMap.<Type, Type>builder()
            .put(Integer.class, int.class)
            .put(Short.class, short.class)
            .put(Long.class, long.class)
            .put(Float.class, float.class)
            .put(Double.class, double.class)
            .put(Byte.class, byte.class)
            .put(Boolean.class, boolean.class)
            .put(Character.class, char.class)
            .put(Void.class, void.class)
            .build();

    private Type unboxed(Type boxed) {
        return unboxed.get(boxed);
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
