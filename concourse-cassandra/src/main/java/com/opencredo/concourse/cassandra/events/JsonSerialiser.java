package com.opencredo.concourse.cassandra.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Function;

public final class JsonSerialiser implements Function<Object, String> {

    public static JsonSerialiser using(ObjectMapper objectMapper) {
        return new JsonSerialiser(objectMapper);
    }

    private final ObjectMapper objectMapper;

    private JsonSerialiser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String apply(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
