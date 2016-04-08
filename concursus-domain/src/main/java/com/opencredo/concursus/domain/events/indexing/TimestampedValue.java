package com.opencredo.concursus.domain.events.indexing;

import java.util.Objects;

final class TimestampedValue<V, T extends Comparable<T>> {
    private final T timestamp;
    private final V value;

    TimestampedValue(T timestamp, V value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public boolean isBefore(TimestampedValue<V, T> other) {
        return other.timestamp.compareTo(timestamp) > 0;
    }

    public V getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return this == o ||
                (o instanceof TimestampedValue
                        && ((TimestampedValue) o).timestamp.equals(timestamp)
                        && ((TimestampedValue) o).value.equals(value));
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, value);
    }

    @Override
    public String toString() {
        return value + "@" + timestamp;
    }
}
