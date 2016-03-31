package com.opencredo.concursus.data.tuples;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

import java.util.List;
import java.util.Map;
import java.util.Optional;

final class Types {

    private Types() {
    }

    static <T> TypeToken<List<T>> listOf(Class<T> elementType) {
        return new TypeToken<List<T>>() {}
                .where(new TypeParameter<T>() {}, elementType);
    }

    static <K, V> TypeToken<Map<K, V>> mapOf(Class<K> keyType, Class<V> valueType) {
        return new TypeToken<Map<K, V>>() {}
                .where(new TypeParameter<K>() {}, keyType)
                .where(new TypeParameter<V>() {}, valueType);
    }

    static <T> TypeToken<Optional<T>> optionalOf(Class<T> valueType) {
        return new TypeToken<Optional<T>>() {}
                .where(new TypeParameter<T>() {}, valueType);
    }

}
