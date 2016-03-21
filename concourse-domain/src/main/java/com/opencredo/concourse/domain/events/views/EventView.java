package com.opencredo.concourse.domain.events.views;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.time.TimeUUID;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * A view of an {@link Event} suitable for serialisation, e.g. into JSON.
 */
public final class EventView {

    /**
     * Construact an {@link EventView} representing the data of the supplied {@link Event}.
     * @param event The {@link Event} to create a view of.
     * @return The created {@link EventView}.
     */
    public static EventView of(Event event) {
        return new EventView(
                event.getAggregateId().getType(),
                event.getAggregateId().getId(),
                toDate(event.getEventTimestamp().getTimestamp()),
                event.getEventTimestamp().getStreamId(),
                event.getProcessingId().map(pid -> toDate(TimeUUID.getInstant(pid))).orElse(null),
                event.getProcessingId().orElse(null),
                event.getEventName().getName(),
                event.getEventName().getVersion(),
                event.getParameters().toMap()
        );
    }

    private static String toDate(Instant instant) {
        return instant.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private final String aggregateType;
    private final UUID aggregateId;
    private final String eventTimestamp;
    private final String eventStreamId;
    private final String processingTimestamp;
    private final UUID processingId;
    private final String eventName;
    private final String eventVersion;
    private final Map<String, Object> parameters;

    private EventView(String aggregateType, UUID aggregateId, String eventTimestamp, String eventStreamId, String processingTimestamp, UUID processingId, String eventName, String eventVersion, Map<String, Object> parameters) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventTimestamp = eventTimestamp;
        this.eventStreamId = eventStreamId;
        this.processingTimestamp = processingTimestamp;
        this.processingId = processingId;
        this.eventName = eventName;
        this.eventVersion = eventVersion;
        this.parameters = parameters;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getEventTimestamp() {
        return eventTimestamp;
    }

    public String getEventStreamId() {
        return eventStreamId;
    }

    public String getProcessingTimestamp() {
        return processingTimestamp;
    }

    public UUID getProcessingId() {
        return processingId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventVersion() {
        return eventVersion;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
