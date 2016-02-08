package com.opencredo.concourse.data.tuples;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A slot in a TupleSchema which has a name and a type.
 */
public final class TupleSlot {

    public static <T> TupleSlot ofList(String name, Class<T> elementType) {
        checkNotNull(elementType, "elementType must not be null");

        return of(name, Types.listOf(elementType).getType());
    }

    public static <T> TupleSlot ofOptional(String name, Class<T> valueType) {
        checkNotNull(valueType, "valueType must not be null");

        return of(name, Types.optionalOf(valueType).getType());
    }

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

    private Type unboxed(Type boxed) {
        return boxed == Integer.class
                ? int.class
                : boxed == Short.class
                    ? short.class
                    : boxed == Long.class
                        ? long.class
                        : boxed == Float.class
                            ? float.class
                            : boxed == Double.class
                                ? double.class
                                : boxed;
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
