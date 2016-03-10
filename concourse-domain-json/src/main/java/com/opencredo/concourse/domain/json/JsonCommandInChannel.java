package com.opencredo.concourse.domain.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concourse.domain.commands.Command;
import com.opencredo.concourse.domain.commands.CommandTypeMatcher;
import com.opencredo.concourse.domain.commands.channels.CommandInChannel;
import com.opencredo.concourse.domain.commands.dispatching.CommandBus;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class JsonCommandInChannel implements CommandInChannel<String, String> {

    public static JsonCommandInChannel using(CommandTypeMatcher commandTypeMatcher, ObjectMapper objectMapper, CommandBus commandBus) {
        return new JsonCommandInChannel(commandTypeMatcher, objectMapper, commandBus);
    }

    private final CommandTypeMatcher commandTypeMatcher;
    private final ObjectMapper objectMapper;
    private final CommandBus commandBus;

    private JsonCommandInChannel(CommandTypeMatcher commandTypeMatcher, ObjectMapper objectMapper, CommandBus commandBus) {
        this.commandTypeMatcher = commandTypeMatcher;
        this.objectMapper = objectMapper;
        this.commandBus = commandBus;
    }

    @Override
    public CompletableFuture<String> apply(String json) {
        try {
            CommandJson commandJson = objectMapper.readValue(json, CommandJson.class);
            Command command = commandJson.toCommand(commandTypeMatcher, objectMapper)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Command type " +
                                    commandJson.getCommandType() +
                                    " not recognised"));

            return commandBus.dispatchAsync(command).thenApply(this::serialiseResult);
        } catch (Exception e) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    private String serialiseResult(Optional<Object> result) {
        try {
            return objectMapper.writeValueAsString(result.orElse(null));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
