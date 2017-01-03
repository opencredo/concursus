package com.opencredo.concursus.data.tuples;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

final class Types {

    private Types() {
    }

    private static abstract class ConstructedParameterizedType implements ParameterizedType {

        @Override
        public boolean equals(Object o) {
            return o instanceof ParameterizedType && equals(ParameterizedType.class.cast(o));
        }

        private boolean equals(ParameterizedType o) {
            return Arrays.deepEquals(getActualTypeArguments(), o.getActualTypeArguments())
                    && Objects.equals(getRawType(), o.getRawType());
        }

        @Override
        public int hashCode() {
            return Objects.hash(Arrays.deepHashCode(getActualTypeArguments()), getRawType());
        }

        @Override
        public String toString() {
            return getTypeName();
        }
    }

    private static final class ListType extends ConstructedParameterizedType {
        private final Type elementType;

        private ListType(Type elementType) {
            this.elementType = elementType;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[] {elementType};
        }

        @Override
        public Type getRawType() {
            return List.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }

        @Override
        public String getTypeName() {
            return String.format("List<%s>", elementType.getTypeName());
        }
    }

    private static final class OptionalType extends ConstructedParameterizedType {
        private final Type valueType;

        private OptionalType(Type valueType) {
            this.valueType = valueType;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[] {valueType};
        }

        @Override
        public Type getRawType() {
            return Optional.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }

        @Override
        public String getTypeName() {
            return String.format("Optional<%s>", valueType.getTypeName());
        }
    }

    private static final class MapType extends ConstructedParameterizedType  {
        private final Type keyType;
        private final Type valueType;

        private MapType(Type keyType, Type valueType) {
            this.keyType = keyType;
            this.valueType = valueType;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[] {keyType, valueType};
        }

        @Override
        public Type getRawType() {
            return Map.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }

        @Override
        public String getTypeName() {
            return String.format("Map<%s, %s>", keyType.getTypeName(), valueType.getTypeName());
        }
    }

    static <T> Type listOf(Class<T> elementType) {
        return new ListType(elementType);
    }

    static <K, V> Type mapOf(Class<K> keyType, Class<V> valueType) {
        return new MapType(keyType, valueType);
    }

    static <T> Type optionalOf(Class<T> valueType) {
        return new OptionalType(valueType);
    }

    static boolean isAssignableFrom(Type targetType, Type valueType) {
        return targetType.equals(valueType) ||
                (valueType instanceof Class && isAssignableFrom(targetType, Class.class.cast(valueType)));
    }

    private static boolean isAssignableFrom(Type targetType, Class<?> valueType) {
        return (targetType instanceof Class && isAssignableFrom(Class.class.cast(targetType), valueType)
            || targetType instanceof ParameterizedType
                        && isAssignableFrom(
                            Class.class.cast(ParameterizedType.class.cast(targetType).getRawType()),
                            valueType));
    }

    private static boolean isAssignableFrom(Class<?> targetClass, Class<?> valueClass) {
        return targetClass.isAssignableFrom(valueClass)
                || targetClass.isPrimitive() && targetClass.isAssignableFrom(unboxed.get(valueClass));
    }

    private static final Map<Class<?>, Class<?>> unboxed = new HashMap<>();
    static {
        unboxed.put(Boolean.class, boolean.class);
        unboxed.put(Byte.class, byte.class);
        unboxed.put(Character.class, char.class);
        unboxed.put(Integer.class, int.class);
        unboxed.put(Short.class, short.class);
        unboxed.put(Long.class, long.class);
        unboxed.put(Float.class, float.class);
        unboxed.put(Double.class, double.class);
    }

}
