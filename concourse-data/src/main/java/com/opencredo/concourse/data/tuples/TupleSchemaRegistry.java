package com.opencredo.concourse.data.tuples;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.Optional;

public final class TupleSchemaRegistry {

    private final BiMap<String, TupleSchema> nameToSchema = HashBiMap.create();
    private final BiMap<TupleSchema, String> schemaToName = nameToSchema.inverse();

    public TupleSchemaRegistry add(String name, TupleSchema tupleSchema) {
        nameToSchema.put(name, tupleSchema);
        return this;
    }

    public TupleSchema create(String name, TupleSlot...tupleSlots) {
        TupleSchema schema = TupleSchema.of(tupleSlots);
        add(name, schema);
        return schema;
    }

    public Optional<String> getName(TupleSchema schema) {
        return Optional.ofNullable(schemaToName.get(schema));
    }

    public Optional<TupleSchema> getSchema(String name) {
        return Optional.ofNullable(nameToSchema.get(name));
    }
}
