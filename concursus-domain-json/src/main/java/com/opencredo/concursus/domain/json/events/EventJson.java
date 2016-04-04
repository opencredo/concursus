package com.opencredo.concursus.domain.json.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.opencredo.concursus.domain.common.VersionedName;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.EventType;
import com.opencredo.concursus.domain.events.matching.EventTypeMatcher;
import com.opencredo.concursus.domain.time.StreamTimestamp;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Representation of an {@link Event}'s data in JSON-serialisable form.
 */
public final class EventJson {

    /**
     * Serialise the supplied {@link Event} to a string using the supplied {@link ObjectMapper}.
     * @param event The {@link Event} to serialise.
     * @param objectMapper The {@link ObjectMapper} to use.
     * @return The JSON-serialised {@link Event}, as a string.
     */
    public static String toString(Event event, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(of(event, objectMapper));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deserialised the supplied JSON event string to an {@link Event}, using the supplied {@link EventTypeMatcher} and
     * {@link ObjectMapper}.
     * @param eventString The JSON event string to deserialise.
     * @param eventTypeMatcher The {@link EventTypeMatcher} to use to resolve {@link EventType}s to
     * {@link com.opencredo.concursus.data.tuples.TupleSchema}s
     * @param objectMapper The {@link ObjectMapper} to use to deserialise event parameters.
     * @return The converted {@link Event}, iff the {@link EventTypeMatcher} matches its type.
     */
    public static Optional<Event> fromString(String eventString, EventTypeMatcher eventTypeMatcher, ObjectMapper objectMapper) {
        try {
            EventJson eventJson = objectMapper.readValue(eventString, EventJson.class);
            return eventJson.toEvent(eventTypeMatcher, objectMapper);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert an {@link Event} to a serialisable {@link EventJson} object, using the supplied {@link ObjectMapper}
     * to serialise the event's parameters to JSON nodes.
     * @param event The {@link Event} to serialise.
     * @param objectMapper The {@link ObjectMapper} to use to serialise the event's parameters to JSON nodes.
     * @return The mapped {@link EventJson}.
     */
    public static EventJson of(Event event, ObjectMapper objectMapper) {
        Function<Object, JsonNode> serialiser = objectMapper::valueToTree;
        return of(
                event.getAggregateId().getType(),
                event.getAggregateId().getId().toString(),
                event.getEventName().getName(),
                event.getEventName().getVersion(),
                event.getEventTimestamp().getTimestamp().toEpochMilli(),
                event.getEventTimestamp().getStreamId(),
                event.getProcessingId().map(UUID::toString).orElse(""),
                event.getCharacteristics(),
                event.getParameters().serialise(serialiser)
        );
    }

    /**
     * Create an {@link EventJson} object from its properties. Used by Jackson to deserialise event JSON.
     * @param aggregateType
     * @param aggregateId
     * @param name
     * @param version
     * @param eventTimestamp
     * @param streamId
     * @param processingId
     * @param characteristics
     * @param parameters
     * @return The constructed {@link EventJson} object.
     */
    @JsonCreator
    public static EventJson of(String aggregateType, String aggregateId, String name, String version, long eventTimestamp, String streamId, String processingId, int characteristics, Map<String, JsonNode> parameters) {
        return new EventJson(aggregateType, aggregateId, name, version, eventTimestamp, streamId, processingId, characteristics, parameters);
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

    @JsonProperty
    private final Map<String, JsonNode> parameters;

    private EventJson(String aggregateType, String aggregateId, String name, String version, long eventTimestamp, String streamId, String processingId, int characteristics, Map<String, JsonNode> parameters) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.name = name;
        this.version = version;
        this.eventTimestamp = eventTimestamp;
        this.streamId = streamId;
        this.processingId = processingId;
        this.characteristics = characteristics;
        this.parameters = parameters;
    }

    /**
     * Convert this {@link EventJson} to an {@link Event}, using the supplied {@link EventTypeMatcher} and
     * {@link ObjectMapper}.
     * @param typeMatcher The {@link EventTypeMatcher} to use to resolve {@link EventType}s to
     * {@link com.opencredo.concursus.data.tuples.TupleSchema}s
     * @param objectMapper The {@link ObjectMapper} to use to deserialise event parameters.
     * @return The converted {@link Event}, iff the {@link EventTypeMatcher} matches its type.
     */
    public Optional<Event> toEvent(EventTypeMatcher typeMatcher, ObjectMapper objectMapper) {
        EventType eventType = EventType.of(aggregateType, VersionedName.of(name, version));

        BiFunction<JsonNode, Type, Object> deserialiser = makeDeserialiser(objectMapper);

        return typeMatcher.match(eventType).map(tupleSchema ->
            eventType.makeEvent(
                    UUID.fromString(aggregateId),
                    StreamTimestamp.of(streamId, Instant.ofEpochMilli(eventTimestamp)),
                    tupleSchema.deserialise(deserialiser, parameters),
                    characteristics
            ))
            .map(event -> processingId.isEmpty() ? event : event.processed(UUID.fromString(processingId))
        );
    }

    private BiFunction<JsonNode, Type, Object> makeDeserialiser(ObjectMapper mapper) {
        final TypeFactory typeFactory = mapper.getTypeFactory();
        return (node, type) -> {
            JavaType javaType = typeFactory.constructType(type);
            try {
                final JsonParser jsonParser = mapper.treeAsTokens(node);
                final ObjectCodec codec = jsonParser.getCodec();

                return codec.readValue(jsonParser, javaType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
