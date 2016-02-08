package com.opencredo.concourse.domain;

import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public final class AggregateId {

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

    public String getType() {
        return type;
    }

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
