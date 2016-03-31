package com.opencredo.concursus.domain.events;

import com.opencredo.concursus.data.tuples.Tuple;
import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.common.VersionedName;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.domain.time.TimeUUID;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event in the system.
 */
public final class Event {

    /**
     * Create a processed {@link Event} with the supplied properties.
     * @param aggregateId The {@link AggregateId} of the aggregate to which the event occurred.
     * @param eventTimestamp The {@link StreamTimestamp} representing the time at which the event occurred.
     * @param processingId The processing {@link UUID} of the event (should be present only if the event has actually
     *                     been processed). This must be a type 1 UUID, as it encodes the event's processing timestamp.
     * @param eventName The {@link VersionedName} of the event.
     * @param parameters The event's parameters.
     * @param characteristics The event's characteristics, e.g. whether it is an initial or terminal event.
     * @return The constructed {@link Event}.
     */
    public static Event of(AggregateId aggregateId, StreamTimestamp eventTimestamp, UUID processingId, VersionedName eventName, Tuple parameters, int...characteristics) {
        checkNotNull(processingId, "processingId must not be null");
        checkArgument(processingId.version() == 1, "processingId must be type 1 UUID");

        return of(aggregateId, eventTimestamp, Optional.of(processingId), eventName, parameters, characteristics);
    }

    /**
     * Create an unprocessed {@link Event} with the supplied properties.
     * @param aggregateId The {@link AggregateId} of the aggregate to which the event occurred.
     * @param eventTimestamp The {@link StreamTimestamp} representing the time at which the event occurred.
     * @param eventName The {@link VersionedName} of the event.
     * @param parameters The event's parameters.
     * @param characteristics The event's characteristics, e.g. whether it is an initial or terminal event.
     * @return The constructed {@link Event}.
     */
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

    /**
     * Make a copy of this {@link Event} with the processing Id set to the supplied processing id.
     * @param processingId The processing {@link UUID} of the event. This must be a type 1 UUID, as it encodes the
     *                     event's processing timestamp.
     * @return The updated {@link Event}.
     */
    public Event processed(UUID processingId) {
        checkNotNull(processingId, "processingId must not be null");
        checkArgument(processingId.version() == 1, "processingId must be type 1 UUID");

        return new Event(aggregateId, eventTimestamp, Optional.of(processingId), eventName, parameters, characteristics);
    }

    /**
     * Get the processing time of this event.
     * @return An {@link Optional} containing {@link Instant} at which the event was processed, or
     * {@link Optional}::empty if it has not.
     */
    public Optional<Instant> getProcessingTime() {
        return processingId.map(TimeUUID::getInstant);
    }

    /**
     * Get the {@link AggregateId} of the aggregate to which this event occurred.
     * @return The {@link AggregateId} of the aggregate to which this event occurred.
     */
    public AggregateId getAggregateId() {
        return aggregateId;
    }

    /**
     * Get the {@link StreamTimestamp} of the time at which this event occurred.
     * @return The {@link StreamTimestamp} of the time at which this event occurred.
     */
    public StreamTimestamp getEventTimestamp() {
        return eventTimestamp;
    }

    /**
     * Get the processing ID of this event (if it has been processed).
     * @return An {@link Optional} containing the event's processing {@link UUID} if it has been processed, or
     * {@link Optional}::empty if it has not.
     */
    public Optional<UUID> getProcessingId() {
        return processingId;
    }

    /**
     * Get the {@link VersionedName} of the event.
     * @return The {@link VersionedName} of the event
     */
    public VersionedName getEventName() {
        return eventName;
    }

    /**
     * Get the event's parameters.
     * @return The event's parameters, encoded as a {@link Tuple}.
     */
    public Tuple getParameters() {
        return parameters;
    }

    /**
     * Get the event's characteristics.
     * @return The event's characteristics, encoded as an {@link int} bitfield.
     */
    public int getCharacteristics() {
        return characteristics;
    }

    /**
     * Test whether the event has the given characteristic.
     * @param characteristic The characteristic to test for.
     * @return True if the event has the given characteristic, false otherwise.
     */
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
