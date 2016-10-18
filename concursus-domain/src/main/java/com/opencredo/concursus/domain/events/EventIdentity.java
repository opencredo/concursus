package com.opencredo.concursus.domain.events;

import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.time.StreamTimestamp;

import java.util.Objects;

/**
 * The identity of an {@link Event}, consisting of its {@link AggregateId} and {@link StreamTimestamp}.
 */
public final class EventIdentity {

    public static EventIdentity of(AggregateId aggregateId, StreamTimestamp streamTimestamp) {
        return new EventIdentity(aggregateId, streamTimestamp);
    }

    private final AggregateId aggregateId;
    private final StreamTimestamp streamTimestamp;

    private EventIdentity(AggregateId aggregateId, StreamTimestamp streamTimestamp) {
        this.aggregateId = aggregateId;
        this.streamTimestamp = streamTimestamp;
    }

    /**
     * Get the {@link AggregateId} of the {@link Event} whose identity this object represents.
     * @return The {@link AggregateId} of the {@link Event} whose identity this object represents.
     */
    public AggregateId getAggregateId() {
        return aggregateId;
    }

    /**
     * Get the {@link StreamTimestamp} of the {@link Event} whose identity this object represents.
     * @return The {@link StreamTimestamp} of the {@link Event} whose identity this object represents.
     */
    public StreamTimestamp getStreamTimestamp() {
        return streamTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        return this == o
                || ((o instanceof EventIdentity)
                    && ((EventIdentity) o).aggregateId.equals(aggregateId)
                    && ((EventIdentity) o).streamTimestamp.equals(streamTimestamp));
    }

    @Override
    public int hashCode() {
        return Objects.hash(aggregateId, streamTimestamp);
    }

    @Override
    public String toString() {
        return aggregateId + "@" + streamTimestamp;
    }
}
