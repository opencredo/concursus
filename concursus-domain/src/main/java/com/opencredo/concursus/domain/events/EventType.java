package com.opencredo.concursus.domain.events;

import com.opencredo.concursus.data.tuples.Tuple;
import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.common.VersionedName;
import com.opencredo.concursus.domain.time.StreamTimestamp;

import java.util.Objects;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The type of an event in the system.
 */
public final class EventType {

    /**
     * The {@link EventType} with the given aggregateType and eventName
     * @param aggregateType The type of aggregate to which this event occurs.
     * @param eventName The name of the event.
     * @return The constructed {@link EventType}.
     */
    public static EventType of(String aggregateType, VersionedName eventName, int...characteristics) {
        checkNotNull(aggregateType, "aggregateType must not be null");
        checkNotNull(eventName, "eventName must not be null");

        return new EventType(aggregateType, eventName, IntStream.of(characteristics).reduce((l, r) -> l & r).orElse(0));
    }

    private final String aggregateType;
    private final VersionedName eventName;
    private final int characteristics;

    private EventType(String aggregateType, VersionedName eventName, int characteristics) {
        this.aggregateType = aggregateType;
        this.eventName = eventName;
        this.characteristics = characteristics;
        System.out.println(this);
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public VersionedName getEventName() {
        return eventName;
    }

    public int getCharacteristics() {
        return characteristics;
    }

    /**
     * Test whether the event type has the given characteristic.
     * @param characteristic The characteristic to test for.
     * @return True if the event type has the given characteristic, false otherwise.
     */
    public boolean hasCharacteristic(int characteristic) {
        return (characteristics & characteristic) > 0;
    }

    @Override
    public boolean equals(Object o) {
        return this == o ||
                (o instanceof EventType
                        && ((EventType) o).aggregateType.equals(aggregateType)
                        && ((EventType) o).eventName.equals(eventName));
    }

    @Override
    public int hashCode() {
        return Objects.hash(aggregateType, eventName);
    }

    @Override
    public String toString() {
        return String.format("%s/%s", aggregateType, eventName.getFormatted());
    }

    /**
     * Create an {@link Event} of this type.
     * @param aggregateId The aggregate id of the event.
     * @param streamTimestamp The {@link StreamTimestamp} of the event.
     * @param parameters The event parameters.
     * @return The constructed {@link Event}.
     */
    public Event makeEvent(String aggregateId, StreamTimestamp streamTimestamp, Tuple parameters) {
        return Event.of(
                EventMetadata.of(
                        this,
                        EventIdentity.of(AggregateId.of(aggregateType, aggregateId), streamTimestamp)),
                        parameters);
    }
}
