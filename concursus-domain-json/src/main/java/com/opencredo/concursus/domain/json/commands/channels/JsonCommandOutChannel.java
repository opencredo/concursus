package com.opencredo.concursus.domain.json.commands.channels;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.channels.CommandInChannel;
import com.opencredo.concursus.domain.commands.channels.CommandOutChannel;
import com.opencredo.concursus.domain.json.commands.CommandJson;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class JsonCommandOutChannel implements CommandOutChannel {

    public static JsonCommandOutChannel using(ObjectMapper objectMapper, CommandInChannel<String, String> inChannel) {
        return new JsonCommandOutChannel(objectMapper, inChannel);
    }

    private final ObjectMapper objectMapper;
    private final CommandInChannel<String, String> inChannel;

    private JsonCommandOutChannel(ObjectMapper objectMapper, CommandInChannel<String, String> inChannel) {
        this.objectMapper = objectMapper;
        this.inChannel = inChannel;
    }

    @Override
    public CompletableFuture<Optional<Object>> apply(Command command) {
        String json = serialise(command);
        return inChannel.apply(json).thenApply(deserialiseTo(command.getResultType()));
    }

    private String serialise(Command command) {
        return CommandJson.toString(command, objectMapper);
    }

    private Function<String, Optional<Object>> deserialiseTo(Type resultType) {
        JavaType javaType = objectMapper.getTypeFactory().constructType(resultType);
        return json -> {
            try {
                return Optional.ofNullable(objectMapper.readValue(json, javaType));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
