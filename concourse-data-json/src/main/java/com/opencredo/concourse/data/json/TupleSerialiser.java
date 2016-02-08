package com.opencredo.concourse.data.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.opencredo.concourse.data.tuples.Tuple;
import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.data.tuples.TupleSchemaRegistry;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class TupleSerialiser extends JsonSerializer<Tuple> {

    private final TupleSchemaRegistry registry;

    public TupleSerialiser(TupleSchemaRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void serialize(Tuple tuple, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        writeSchemaName(jsonGenerator, tuple.getSchema());

        tuple.toMap().entrySet().stream().forEach(writeEntryWith(jsonGenerator));

        jsonGenerator.writeEndObject();
    }

    private Consumer<Entry<String, Object>> writeEntryWith(JsonGenerator jsonGenerator) {
        return entry -> {
            try {
                jsonGenerator.writeFieldName(entry.getKey());
                jsonGenerator.writeObject(entry.getValue());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private void writeSchemaName(JsonGenerator jsonGenerator, TupleSchema schema) throws IOException {
        jsonGenerator.writeStringField(
                "_tupleType",
                registry.getName(schema).orElseThrow(() ->
                        new IllegalStateException(
                                "Schema " + schema + " not found in registry")));
    }
}
