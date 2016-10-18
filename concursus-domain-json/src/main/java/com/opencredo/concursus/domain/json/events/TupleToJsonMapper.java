package com.opencredo.concursus.domain.json.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concursus.data.tuples.Tuple;

import java.util.Map;
import java.util.function.Function;

public final class TupleToJsonMapper implements Function<Tuple, Map<String, JsonNode>> {

    public static TupleToJsonMapper using(ObjectMapper mapper) {
        return new TupleToJsonMapper(mapper);
    }

    private final ObjectMapper mapper;

    public TupleToJsonMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Map<String, JsonNode> apply(Tuple tuple) {
        return tuple.serialise(mapper::valueToTree);
    }
}
