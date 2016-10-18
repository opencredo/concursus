package com.opencredo.concursus.domain.events;

import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.common.VersionedName;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.domain.time.TimeUUID;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The metadata of an event in the system.
 */
public interface EventMetadata {

    static EventMetadata of(EventType eventType, EventIdentity eventIdentity) {
        return of(eventType, eventIdentity, Optional.empty());
    }

    static EventMetadata of(EventType eventType, EventIdentity eventIdentity, UUID processingId) {
        checkNotNull(processingId, "processingId must not be null");
        checkArgument(processingId.version() == 1, "processingId must be type 1 UUID");

        return of(eventType, eventIdentity, Optional.of(processingId));
    }

    static EventMetadata of(EventType eventType, EventIdentity eventIdentity, Optional<UUID> processingId) {
        checkNotNull(eventType, "eventType must not be null");
        checkNotNull(eventIdentity, "eventIdentity must not be null");
        checkNotNull(processingId, "processignId must not be null");
        return new Concrete(eventType, eventIdentity, processingId);
    }

    /**
     * Get the {@link EventType} of this event.
     * @return The {@link EventType} of this event.
     */
    EventType getType();

    /**
     * Get the {@link EventIdentity} of this event.
     * @return The {@link EventIdentity} of this event.
     */
    EventIdentity getIdentity();

    /**
     * Get the processing ID of this event (if it has been processed).
     * @return An {@link Optional} containing the event's processing {@link UUID} if it has been processed, or
     * {@link Optional}::empty if it has not.
     */
    Optional<UUID> getProcessingId();

    class Concrete implements EventMetadata {
        private final EventType eventType;
        private final EventIdentity eventIdentity;
        private final Optional<UUID> processingId;

        private Concrete(EventType eventType, EventIdentity eventIdentity, Optional<UUID> processingId) {
            this.eventType = eventType;
            this.eventIdentity = eventIdentity;
            this.processingId = processingId;
        }

        @Override public EventType getType() {
            return eventType;
        }
        @Override public EventIdentity getIdentity() {
            return eventIdentity;
        }
        @Override public Optional<UUID> getProcessingId() { return processingId; }

        @Override
        public boolean equals(Object o) {
            return this == o || (o instanceof EventMetadata && equals(EventMetadata.class.cast(o)));
        }

        private boolean equals(EventMetadata o) {
            return eventType.equals(o.getType())
                    && eventIdentity.equals(o.getIdentity())
                    && processingId.equals(o.getProcessingId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventType, eventIdentity, processingId);
        }

        @Override
        public String toString() {
            return getProcessingTime().map(processingTime ->
                    String.format("%s:%s\nprocessed at %s",
                            eventType, eventIdentity, processingTime))
                    .orElseGet(() -> String.format("%s%s",
                            eventType, eventIdentity));
        }
    }


    /**
     * Get the processing time of this event.
     * @return An {@link Optional} containing {@link Instant} at which the event was processed, or
     * {@link Optional}::empty if it has not.
     */
    default Optional<Instant> getProcessingTime() {
        return getProcessingId().map(TimeUUID::getInstant);
    }

    /**
     * Get the {@link AggregateId} of the aggregate to which this event occurred.
     * @return The {@link AggregateId} of the aggregate to which this event occurred.
     */
    default AggregateId getAggregateId() {
        return getIdentity().getAggregateId();
    }

    /**
     * Get the {@link StreamTimestamp} of the time at which this event occurred.
     * @return The {@link StreamTimestamp} of the time at which this event occurred.
     */
    default StreamTimestamp getEventTimestamp() {
        return getIdentity().getStreamTimestamp();
    }


    /**
     * Get the {@link VersionedName} of the event.
     * @return The {@link VersionedName} of the event
     */
    default VersionedName getEventName() {
        return getType().getEventName();
    }

    /**
     * Get the event's characteristics.
     * @return The event's characteristics, encoded as an {@link int} bitfield.
     */
    default int getCharacteristics() {
        return getType().getCharacteristics();
    }

    /**
     * Test whether the event has the given characteristic.
     * @param characteristic The characteristic to test for.
     * @return True if the event has the given characteristic, false otherwise.
     */
    default boolean hasCharacteristic(int characteristic) {
        return getType().hasCharacteristic(characteristic);
    }

    /**
     * Make a copy of this {@link EventMetadata} with the processing Id set to the supplied processing id.
     * @param processingId The processing {@link UUID} of the event. This must be a type 1 UUID, as it encodes the
     *                     event's processing timestamp.
     * @return The updated {@link EventMetadata}.
     */
    default EventMetadata processed(UUID processingId) {
        checkNotNull(processingId, "processingId must not be null");
        checkArgument(processingId.version() == 1, "processingId must be type 1 UUID");

        return of(getType(), getIdentity(), Optional.of(processingId));
    }


}
