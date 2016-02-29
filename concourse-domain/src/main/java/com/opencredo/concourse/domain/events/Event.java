package com.opencredo.concourse.domain.events;

import com.opencredo.concourse.data.tuples.Tuple;
import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.common.VersionedName;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.domain.time.TimeUUID;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Event {

    public static Event of(AggregateId aggregateId, StreamTimestamp eventTimestamp, UUID processingId, VersionedName eventName, Tuple parameters, int...characteristics) {
        checkNotNull(processingId, "processingId must not be null");
        checkArgument(processingId.version() == 1, "processingId must be type 1 UUID");

        return of(aggregateId, eventTimestamp, Optional.of(processingId), eventName, parameters, characteristics);
    }

    public static Event of(AggregateId aggregateId, StreamTimestamp eventTimestamp, VersionedName eventName, Tuple parameters, int...characteristics) {
        return of(aggregateId, eventTimestamp, Optional.empty(), eventName, parameters, characteristics);
    }

    private static Event of(AggregateId aggregateId, StreamTimestamp eventTimestamp, Optional<UUID> processingId, VersionedName eventName, Tuple parameters, int...characteristics) {
        checkNotNull(aggregateId, "aggregateId must not be null");
        checkNotNull(eventTimestamp, "eventTimestamp must not be null");
        checkNotNull(eventName, "eventName must not be null");
        checkNotNull(parameters, "parameters must not be null");

        return new Event(aggregateId, eventTimestamp, processingId, eventName, parameters, IntStream.of(characteristics).reduce((l, r) -> l & r).orElse(0));
    }

    private final AggregateId aggregateId;
    private final StreamTimestamp eventTimestamp;

    private final Optional<UUID> processingId;

    private final VersionedName eventName;
    private final Tuple parameters;

    private final int characteristics;

    private Event(AggregateId aggregateId, StreamTimestamp eventTimestamp, Optional<UUID> processingId, VersionedName eventName, Tuple parameters, int characteristics) {
        this.aggregateId = aggregateId;
        this.eventTimestamp = eventTimestamp;
        this.processingId = processingId;
        this.eventName = eventName;
        this.parameters = parameters;
        this.characteristics = characteristics;
    }

    public Event processed(UUID processingId) {
        checkNotNull(processingId, "processingId must not be null");
        checkArgument(processingId.version() == 1, "processingId must be type 1 UUID");

        return new Event(aggregateId, eventTimestamp, Optional.of(processingId), eventName, parameters, characteristics);
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

    public Tuple getParameters() {
        return parameters;
    }

    public int getCharacteristics() {
        return characteristics;
    }

    public boolean hasCharacteristic(int characteristic) {
        return (characteristics & characteristic) > 0;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Event && equals(Event.class.cast(o)));
    }

    private boolean equals(Event o) {
        return aggregateId.equals(o.aggregateId)
                && eventTimestamp.equals(o.eventTimestamp)
                && processingId.equals(o.processingId)
                && eventName.equals(o.eventName)
                && parameters.equals(o.parameters)
                && characteristics == o.characteristics;
    }

    @Override
    public int hashCode() {
        return Objects.hash(aggregateId, eventTimestamp, processingId, eventName, parameters, characteristics);
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
