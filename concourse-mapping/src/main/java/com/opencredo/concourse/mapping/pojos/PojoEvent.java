package com.opencredo.concourse.mapping.pojos;

import com.opencredo.concourse.domain.AggregateId;
import com.opencredo.concourse.domain.StreamTimestamp;
import com.opencredo.concourse.domain.VersionedName;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.time.TimeUUID;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class PojoEvent<T> {

    public static <T> PojoEvent<T> of(Event event, Class<T> pojoClass) {
        return new PojoEvent<>(
                event.getAggregateId(),
                event.getEventTimestamp(),
                event.getProcessingId(),
                event.getEventName(),
                TuplePojo.wrapping(event.getParameters(), pojoClass));
    }

    private final AggregateId aggregateId;
    private final StreamTimestamp eventTimestamp;

    private final Optional<UUID> processingId;

    private final VersionedName eventName;
    private final T parameters;

    private PojoEvent(AggregateId aggregateId, StreamTimestamp eventTimestamp, Optional<UUID> processingId, VersionedName eventName, T parameters) {
        this.aggregateId = aggregateId;
        this.eventTimestamp = eventTimestamp;
        this.processingId = processingId;
        this.eventName = eventName;
        this.parameters = parameters;
    }

    public PojoEvent<T> processed(UUID processingId) {
        checkNotNull(processingId, "processingId must not be null");
        checkArgument(processingId.variant() == 1, "processingId must by type 1 UUID");

        return new PojoEvent<>(aggregateId, eventTimestamp, Optional.of(processingId), eventName, parameters);
    }

    public Optional<Instant> getProcessingTime() {
        return processingId.map(TimeUUID::getInstant);
    }

    public AggregateId getAggregateId() {
        return aggregateId;
    }

    public StreamTimestamp getEventTimestamp() {
        return eventTimestamp;
    }

    public Optional<UUID> getProcessingId() {
        return processingId;
    }

    public VersionedName getEventName() {
        return eventName;
    }

    public T getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof PojoEvent && equals(PojoEvent.class.cast(o)));
    }

    private boolean equals(PojoEvent o) {
        return aggregateId.equals(o.aggregateId)
                && eventTimestamp.equals(o.eventTimestamp)
                && processingId.equals(o.processingId)
                && eventName.equals(o.eventName)
                && parameters.equals(o.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aggregateId, eventTimestamp, processingId, eventName, parameters);
    }

    @Override
    public String toString() {
        return getProcessingTime().map(processingTime ->
                String.format("%s %s\nat %s\nwith %s\nprocessed at %s",
                        aggregateId, eventName, eventTimestamp, parameters, processingTime))
                .orElseGet(() -> String.format("%s %s\nat %s\nwith %s",
                        aggregateId, eventName, eventTimestamp, parameters));
    }
}
