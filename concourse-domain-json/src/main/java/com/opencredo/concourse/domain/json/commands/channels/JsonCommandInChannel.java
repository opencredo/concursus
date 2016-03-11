package com.opencredo.concourse.domain.json.commands.channels;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concourse.domain.commands.Command;
import com.opencredo.concourse.domain.commands.CommandTypeMatcher;
import com.opencredo.concourse.domain.commands.channels.CommandInChannel;
import com.opencredo.concourse.domain.commands.channels.CommandOutChannel;
import com.opencredo.concourse.domain.json.commands.CommandJson;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class JsonCommandInChannel implements CommandInChannel<String, String> {

    public static JsonCommandInChannel using(CommandTypeMatcher commandTypeMatcher, ObjectMapper objectMapper, CommandOutChannel outChannel) {
        return new JsonCommandInChannel(commandTypeMatcher, objectMapper, outChannel);
    }

    private final CommandTypeMatcher commandTypeMatcher;
    private final ObjectMapper objectMapper;
    private final CommandOutChannel outChannel;

    private JsonCommandInChannel(CommandTypeMatcher commandTypeMatcher, ObjectMapper objectMapper, CommandOutChannel outChannel) {
        this.commandTypeMatcher = commandTypeMatcher;
        this.objectMapper = objectMapper;
        this.outChannel = outChannel;
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

            return outChannel.apply(command).thenApply(this::serialiseResult);
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
