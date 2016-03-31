package com.opencredo.concursus.cassandra.events;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.function.BiFunction;

final class JsonDeserialiser implements BiFunction<String, Type, Object> {

    public static JsonDeserialiser using(ObjectMapper objectMapper) {
        return new JsonDeserialiser(objectMapper);
    }

    private final ObjectMapper objectMapper;

    private JsonDeserialiser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Object apply(String s, Type type) {
        JavaType javaType = objectMapper.constructType(type);
        try {
            return objectMapper.readValue(s, javaType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
