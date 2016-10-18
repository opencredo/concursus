package com.opencredo.concursus.domain.json.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.common.VersionedName;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.EventIdentity;
import com.opencredo.concursus.domain.events.EventMetadata;
import com.opencredo.concursus.domain.events.EventType;
import com.opencredo.concursus.domain.time.StreamTimestamp;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Representation of an {@link Event}'s {@link EventMetadata} in JSON-serialisable form.
 */
public final class EventMetadataJson {

    /**
     * Create an {@link EventMetadataJson} object from an {@link EventMetadata} object.
     * @param eventMetadata The {@link EventMetadata} object to translate.
     * @return The translated {@link EventMetadataJson} object.
     */
    public static EventMetadataJson from(EventMetadata eventMetadata) {
        return of(eventMetadata.getAggregateId().getType(),
                eventMetadata.getAggregateId().getId(),
                eventMetadata.getEventName().getName(),
                eventMetadata.getEventName().getVersion(),
                eventMetadata.getEventTimestamp().getTimestamp().toEpochMilli(),
                eventMetadata.getEventTimestamp().getStreamId(),
                eventMetadata.getProcessingId().map(UUID::toString).orElse(""),
                eventMetadata.getCharacteristics());
    }

    /**
     * Create an {@link EventMetadataJson} object from its properties. Used by Jackson to deserialise event JSON.
     * @param aggregateType
     * @param aggregateId
     * @param name
     * @param version
     * @param eventTimestamp
     * @param streamId
     * @param processingId
     * @param characteristics
     * @return The constructed {@link EventMetadataJson} object.
     */
    @JsonCreator
    public static EventMetadataJson of(String aggregateType, String aggregateId, String name, String version, long eventTimestamp, String streamId, String processingId, int characteristics) {
        return new EventMetadataJson(aggregateType, aggregateId, name, version, eventTimestamp, streamId, processingId, characteristics);
    }

    @JsonProperty
    private final String aggregateType;

    @JsonProperty
    private final String aggregateId;

    @JsonProperty
    private final String name;

    @JsonProperty
    private final String version;

    @JsonProperty
    private final long eventTimestamp;

    @JsonProperty
    private final String streamId;

    @JsonProperty
    private final String processingId;

    @JsonProperty
    private final int characteristics;

    private EventMetadataJson(String aggregateType, String aggregateId, String name, String version, long eventTimestamp, String streamId, String processingId, int characteristics) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.name = name;
        this.version = version;
        this.eventTimestamp = eventTimestamp;
        this.streamId = streamId;
        this.processingId = processingId;
        this.characteristics = characteristics;
    }

    /**
     * Convert this {@link EventMetadataJson} to {@link EventMetadata}.
     * @return The converted {@link EventMetadata}.
     */
    public EventMetadata toEventMetadata() {
        return EventMetadata.of(
                EventType.of(aggregateType, VersionedName.of(name, version), characteristics),
                EventIdentity.of(
                        AggregateId.of(aggregateType, aggregateId),
                        StreamTimestamp.of(streamId, Instant.ofEpochMilli(eventTimestamp))),
                Optional.ofNullable(processingId).map(UUID::fromString)
        );
    }

}
