package com.opencredo.concourse.domain.events;

import com.opencredo.concourse.domain.VersionedName;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public final class EventType {

    public static EventType of(Event event) {
        checkNotNull(event, "event must not be null");

        return of(event.getAggregateId().getType(), event.getEventName());
    }

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


}
