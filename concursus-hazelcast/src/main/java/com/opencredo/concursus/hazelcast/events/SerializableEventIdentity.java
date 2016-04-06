package com.opencredo.concursus.hazelcast.events;

import com.opencredo.concursus.domain.events.Event;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

final class SerializableEventIdentity implements Serializable {

    static SerializableEventIdentity of(Event event) {
        return new SerializableEventIdentity(
                event.getAggregateId().getType(),
                event.getAggregateId().getId(),
                event.getEventTimestamp().getTimestamp(),
                event.getEventTimestamp().getStreamId());
    }

    private final String aggregateType;
    private final UUID aggregateId;
    private final Instant eventTimestamp;
    private final String streamId;

    private SerializableEventIdentity(String aggregateType, UUID aggregateId, Instant eventTimestamp, String streamId) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventTimestamp = eventTimestamp;
        this.streamId = streamId;
    }

    @Override
    public boolean equals(Object o) {
        return this == o ||
                (o instanceof SerializableEventIdentity && equals((SerializableEventIdentity) o));
    }

    private boolean equals(SerializableEventIdentity o) {
        return o.aggregateType.equals(aggregateType)
                && o.aggregateId.equals(aggregateId)
                && o.eventTimestamp.equals(eventTimestamp)
                && o.streamId.equals(streamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aggregateType, aggregateId, eventTimestamp, streamId);
    }
}
