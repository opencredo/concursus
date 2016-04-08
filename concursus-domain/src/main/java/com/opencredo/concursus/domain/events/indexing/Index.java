package com.opencredo.concursus.domain.events.indexing;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class Index<K, V> {

    public static <K, V> Index<K, V> create() {
        return create(new ConcurrentHashMap<>());
    }

    public static <K, V> Index<K, V> create(Map<K, Set<V>> indexData) {
        return new Index<>(indexData);
    }

    private final Map<K, Set<V>> indexData;

    private Index(Map<K, Set<V>> indexData) {
        this.indexData = indexData;
    }

    public void add(K key, V value) {
        indexData.compute(key, (k, v) -> {
            Set<V> values = v == null ? new HashSet<>() : v;
            values.add(value);
            return values;
        });
    }

    public void remove(K key, V value) {
        indexData.compute(key, (k, v) -> {
            if (v == null) {
                return null;
            }
            v.remove(value);
            return v.isEmpty() ? null : v;
        });
    }

    public Set<V> get(K key) {
        return indexData.getOrDefault(key, Collections.emptySet());
    }

    @Override
    public String toString() {
        return indexData.toString();
    }
}
