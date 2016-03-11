package com.opencredo.concourse.domain.json.commands;

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
import com.opencredo.concourse.domain.commands.Command;
import com.opencredo.concourse.domain.commands.CommandType;
import com.opencredo.concourse.domain.commands.CommandTypeMatcher;
import com.opencredo.concourse.domain.common.VersionedName;
import com.opencredo.concourse.domain.time.StreamTimestamp;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class CommandJson {

    public static String toString(Command command, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(of(command, objectMapper));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<Command> fromString(String commandString, CommandTypeMatcher commandTypeMatcher, ObjectMapper objectMapper) {
        try {
            CommandJson commandJson = objectMapper.readValue(commandString, CommandJson.class);
            return commandJson.toCommand(commandTypeMatcher, objectMapper);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static CommandJson of(Command command, ObjectMapper objectMapper) {
        Function<Object, JsonNode> serialiser = objectMapper::valueToTree;
        return of(
                command.getAggregateId().getType(),
                command.getAggregateId().getId().toString(),
                command.getCommandName().getName(),
                command.getCommandName().getVersion(),
                command.getCommandTimestamp().getTimestamp().toEpochMilli(),
                command.getCommandTimestamp().getStreamId(),
                command.getProcessingId().map(UUID::toString).orElse(""),
                command.getParameters().serialise(serialiser)
        );
    }

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

    public Optional<Command> toCommand(CommandTypeMatcher typeMatcher, ObjectMapper objectMapper) {
        CommandType commandType = getCommandType();

        BiFunction<JsonNode, Type, Object> deserialiser = makeDeserialiser(objectMapper);

        return typeMatcher.match(commandType).map(typeInfo ->
            commandType.makeCommand(
                    UUID.fromString(aggregateId),
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
