package com.opencredo.concursus.domain.json.commands.channels;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandTypeMatcher;
import com.opencredo.concursus.domain.commands.channels.CommandInChannel;
import com.opencredo.concursus.domain.commands.channels.CommandOutChannel;
import com.opencredo.concursus.domain.functional.CompletableFutures;
import com.opencredo.concursus.domain.json.commands.CommandJson;

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
            return outChannel.apply(deserialiseCommand(json)).thenApply(this::serialiseResult);
        } catch (Exception e) {
            return CompletableFutures.failing(e);
        }
    }

    private Command deserialiseCommand(String json) throws java.io.IOException {
        CommandJson commandJson = objectMapper.readValue(json, CommandJson.class);
        return commandJson.toCommand(commandTypeMatcher, objectMapper)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Command type " +
                                commandJson.getCommandType() +
                                " not recognised"));
    }

    private String serialiseResult(Optional<Object> result) {
        try {
            return objectMapper.writeValueAsString(result.orElse(null));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
