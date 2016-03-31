package com.opencredo.concursus.domain.events;

import com.opencredo.concursus.data.tuples.Tuple;
import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.common.VersionedName;
import com.opencredo.concursus.domain.time.StreamTimestamp;

import java.util.Objects;
import java.util.UUID;

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
    public static EventType of(String aggregateType, VersionedName eventName) {
        checkNotNull(aggregateType, "aggregateType must not be null");
        checkNotNull(eventName, "eventName must not be null");

        return new EventType(aggregateType, eventName);
    }

    private final String aggregateType;
    private final VersionedName eventName;

    private EventType(String aggregateType, VersionedName eventName) {
        this.aggregateType = aggregateType;
        this.eventName = eventName;
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
        return aggregateType + "/" + eventName.getFormatted();
    }

    /**
     * Create an {@link Event} of this type.
     * @param aggregateId The aggregate id of the event.
     * @param streamTimestamp The {@link StreamTimestamp} of the event.
     * @param parameters The event parameters.
     * @param characteristics The characteristics of the event.
     * @return The constructed {@link Event}.
     */
    public Event makeEvent(UUID aggregateId, StreamTimestamp streamTimestamp, Tuple parameters, int...characteristics) {
        return Event.of(
                AggregateId.of(aggregateType, aggregateId),
                streamTimestamp,
                eventName,
                parameters,
                characteristics);
    }
}
