package com.opencredo.concursus.domain.common;

import java.util.Objects;
import java.util.UUID;

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
    public static AggregateId of(String type, UUID id) {
        checkNotNull(type, "type must not be null");
        checkNotNull(id, "id must not be null");

        return new AggregateId(type, id);
    }

    private final String type;
    private final UUID id;

    private AggregateId(String type, UUID id) {
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
     * Get the {@link UUID} of the aggregate bearing this id.
     * @return The {@link UUID} of the aggregate bearing this id.
     */
    public UUID getId() {
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
