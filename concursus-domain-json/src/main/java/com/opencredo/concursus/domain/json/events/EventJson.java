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
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.EventRepresentation;
import com.opencredo.concursus.domain.events.EventType;
import com.opencredo.concursus.domain.events.matching.EventTypeMatcher;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Representation of an {@link Event}'s data in JSON-serialisable form.
 */
public final class EventJson {

    /**
     * Serialise to a string using the supplied {@link ObjectMapper}.
     * @param objectMapper The {@link ObjectMapper} to use.
     * @return The JSON-serialised {@link EventJson}, as a string.
     */
    public String toJsonString(ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deserialised the supplied JSON event string to an {@link Event}, using the supplied {@link EventTypeMatcher} and
     * {@link ObjectMapper}.
     * @param eventString The JSON event string to deserialise.
     * {@link com.opencredo.concursus.data.tuples.TupleSchema}s
     * @param objectMapper The {@link ObjectMapper} to use to deserialise event parameters.
     * @return The converted {@link Event}, iff the {@link EventTypeMatcher} matches its type.
     */
    public static EventJson fromJsonString(String eventString, ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(eventString, EventJson.class);
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
    public static EventJson fromEvent(Event event, ObjectMapper objectMapper) {
        return of(EventMetadataJson.from(event.getMetadata()), TupleToJsonMapper.using(objectMapper).apply(event.getData()));
    }

    public static EventJson fromRepresentation(EventRepresentation<Map<String, JsonNode>> representation) {
        return of(EventMetadataJson.from(representation.getMetadata()), representation.getData());
    }

    /**
     * Create an {@link EventJson} object from its properties. Used by Jackson to deserialise event JSON.
     * @param metadata
     * @param parameters
     * @return The constructed {@link EventJson} object.
     */
    @JsonCreator
    public static EventJson of(EventMetadataJson metadata, Map<String, JsonNode> parameters) {
        return new EventJson(metadata, parameters);
    }

    @JsonProperty
    private final EventMetadataJson metadata;

    @JsonProperty
    private final Map<String, JsonNode> parameters;

    private EventJson(EventMetadataJson metadata, Map<String, JsonNode> parameters) {
        this.metadata = metadata;
        this.parameters = parameters;
    }

    public EventRepresentation<Map<String, JsonNode>> toRepresentation() {
        return EventRepresentation.of(
                metadata.toEventMetadata(),
                parameters
        );
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
        BiFunction<JsonNode, Type, Object> deserialiser = makeDeserialiser(objectMapper);

        return toRepresentation().toEvent(typeMatcher, (schema, nodeMap)
                -> schema.deserialise(deserialiser, nodeMap));
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
