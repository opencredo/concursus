package com.opencredo.concourse.data.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.opencredo.concourse.data.tuples.Tuple;
import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.data.tuples.TupleSchemaRegistry;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public final class TupleDeserialiser extends JsonDeserializer<Tuple> {

    private final TupleSchemaRegistry registry;

    public TupleDeserialiser(TupleSchemaRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Tuple deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        Map<String, JsonNode> topLevel = getJsonMap(parser);

        String tupleType = Optional.ofNullable(topLevel.get("_tupleType"))
                .orElseThrow(() -> new IllegalStateException(topLevel + " has _tupleType"))
                .asText();

        topLevel.remove("_tupleType");

        TupleSchema schema = registry.getSchema(tupleType)
                .orElseThrow(() -> new IllegalStateException("Tuple type " + tupleType + " not found in registry"));

        BiFunction<JsonNode, Type, Object> deserialiser = makeDeserialiser(deserializationContext);

        return schema.deserialise(deserialiser, topLevel);
    }

    private BiFunction<JsonNode, Type, Object> makeDeserialiser(DeserializationContext context) {
        final TypeFactory typeFactory = context.getTypeFactory();
        final ObjectCodec codec = context.getParser().getCodec();

        return (node, type) -> {
            try {
                JsonParser nodeParser = node.traverse(codec);
                nodeParser.nextToken();

                return context.findRootValueDeserializer(typeFactory.constructType(type))
                        .deserialize(nodeParser, context);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Map<String, JsonNode> getJsonMap(JsonParser parser) throws IOException {
        Map<String, JsonNode> result = new HashMap<>();

        if (parser.nextToken() == JsonToken.END_OBJECT) {
            return result;
        }

        while (parser.nextValue() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            result.put(fieldName, parser.readValueAsTree());
        }

        return result;
    }


}
