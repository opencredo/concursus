package com.opencredo.concursus.domain.common;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The identity of an aggregate.
 */
public final class AggregateId {

    /**
     * Create an {@link AggregateId} with the given type and id.
     * @param type The type of the aggregate.
     * @param id The id of the aggregate.
     * @return The constructed {@link AggregateId}
     */
    public static AggregateId of(String type, String id) {
        checkNotNull(type, "type must not be null");
        checkNotNull(id, "id must not be null");

        return new AggregateId(type, id);
    }

    private final String type;
    private final String id;

    private AggregateId(String type, String id) {
        this.type = type;
        this.id = id;
    }

    /**
     * Get the type of the aggregate bearing this id.
     * @return The type of the aggregate bearing this id.
     */
    public String getType() {
        return type;
    }

    /**
     * Get the aggregate id of the aggregate bearing this id.
     * @return The aggregate id of the aggregate bearing this id.
     */
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        return this == o
                || (o instanceof AggregateId
                    && ((AggregateId) o).type.equals(type)
                    && ((AggregateId) o).id.equals(id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }

    @Override
    public String toString() {
        return type + ":" + id;
    }
}
