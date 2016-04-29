package com.opencredo.concursus.domain.json.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandType;
import com.opencredo.concursus.domain.commands.CommandTypeMatcher;
import com.opencredo.concursus.domain.common.VersionedName;
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
 * Representation of a {@link Command}'s data in JSON-serialisable form.
 */
public final class CommandJson {

    /**
     * Serialise the supplied {@link Command} to a JSON string.
     * @param command The {@link Command} to serialise.
     * @param objectMapper The {@link ObjectMapper} to use.
     * @return The serialised command JSON.
     */
    public static String toString(Command command, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(of(command, objectMapper));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deserialise the supplied command string into a {@link Command}.
     * @param commandString The JSON command string to deserialise.
     * @param commandTypeMatcher The {@link CommandTypeMatcher} to use to obtain parameter types for the {@link Command}.
     * @param objectMapper The {@link ObjectMapper} to use.
     * @return The deserialised {@link Command}, iff the {@link CommandType} was matched by the {@link CommandTypeMatcher}.
     */
    public static Optional<Command> fromString(String commandString, CommandTypeMatcher commandTypeMatcher, ObjectMapper objectMapper) {
        try {
            CommandJson commandJson = objectMapper.readValue(commandString, CommandJson.class);
            return commandJson.toCommand(commandTypeMatcher, objectMapper);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert the supplied {@link Command} into a {@link CommandJson} object suitable for JSON serialisation.
     * @param command The {@link Command} to convert.
     * @param objectMapper The {@link ObjectMapper} to use to convert the {@link Command}'s parameters into JSON nodes.
     * @return The converted {@link CommandJson}.
     */
    public static CommandJson of(Command command, ObjectMapper objectMapper) {
        Function<Object, JsonNode> serialiser = objectMapper::valueToTree;
        return of(
                command.getAggregateId().getType(),
                command.getAggregateId().getId(),
                command.getCommandName().getName(),
                command.getCommandName().getVersion(),
                command.getCommandTimestamp().getTimestamp().toEpochMilli(),
                command.getCommandTimestamp().getStreamId(),
                command.getProcessingId().map(UUID::toString).orElse(""),
                command.getParameters().serialise(serialiser)
        );
    }

    /**
     * Create a new {@link CommandJson} object from its properties. Used by Jackson to deserialise command JSON.
     * @param aggregateType
     * @param aggregateId
     * @param name
     * @param version
     * @param commandTimestamp
     * @param streamId
     * @param processingId
     * @param parameters
     * @return
     */
    @JsonCreator
    public static CommandJson of(String aggregateType, String aggregateId, String name, String version, long commandTimestamp, String streamId, String processingId, Map<String, JsonNode> parameters) {
        return new CommandJson(aggregateType, aggregateId, name, version, commandTimestamp, streamId, processingId, parameters);
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
    private final long commandTimestamp;

    @JsonProperty
    private final String streamId;

    @JsonProperty
    private final String processingId;

    @JsonProperty
    private final Map<String, JsonNode> parameters;

    private CommandJson(String aggregateType, String aggregateId, String name, String version, long commandTimestamp, String streamId, String processingId, Map<String, JsonNode> parameters) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.name = name;
        this.version = version;
        this.commandTimestamp = commandTimestamp;
        this.streamId = streamId;
        this.processingId = processingId;
        this.parameters = parameters;
    }

    /**
     * Convert this {@link CommandJson} to an {@link Command}, using the supplied {@link CommandTypeMatcher} and
     * {@link ObjectMapper}.
     * @param typeMatcher The {@link CommandTypeMatcher} to use to resolve {@link CommandType}s to
     * {@link com.opencredo.concursus.domain.commands.CommandTypeInfo}.
     * @param objectMapper The {@link ObjectMapper} to use to deserialise event parameters.
     * @return The converted {@link Command}, iff the {@link CommandTypeMatcher} matches its type.
     */
    public Optional<Command> toCommand(CommandTypeMatcher typeMatcher, ObjectMapper objectMapper) {
        CommandType commandType = getCommandType();

        BiFunction<JsonNode, Type, Object> deserialiser = makeDeserialiser(objectMapper);

        return typeMatcher.match(commandType).map(typeInfo ->
            commandType.makeCommand(
                    aggregateId,
                    StreamTimestamp.of(streamId, Instant.ofEpochMilli(commandTimestamp)),
                    typeInfo.getTupleSchema().deserialise(deserialiser, parameters),
                    typeInfo.getReturnType()
            ))
            .map(command -> processingId.isEmpty() ? command : command.processed(UUID.fromString(processingId))
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

    @JsonIgnore
    public CommandType getCommandType() {
        return CommandType.of(aggregateType, VersionedName.of(name, version));
    }
}
