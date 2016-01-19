package com.opencredo.concourse.data.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.opencredo.concourse.data.tuples.Tuple;
import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.data.tuples.TupleSchemaRegistry;
import com.opencredo.concourse.data.tuples.TupleSlot;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TupleSerialiserTest {

    private final TupleSchema personSchema = TupleSchema.of(
            TupleSlot.of("name", String.class),
            TupleSlot.of("age", Integer.class),
            TupleSlot.of("addresses", new TypeToken<Map<String, Tuple>>() {}.getType())
    );

    private final TupleSchema addressSchema = TupleSchema.of(
            TupleSlot.of("addressLines", new TypeToken<List<String>>() {}.getType()),
            TupleSlot.of("postcode", String.class)
    );

    private final TupleSchemaRegistry registry = new TupleSchemaRegistry()
            .add("person", personSchema)
            .add("address", addressSchema);

    private final TupleSerialiser serialiser = new TupleSerialiser(registry);

    private final ObjectMapper mapper = new ObjectMapper();
    {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Tuple.class, serialiser);
        mapper.registerModule(module);
    }

    @Test
    public void serialisesTupleWithTupleSchemaNameFromRegistry() throws JsonProcessingException {
        Tuple person = personSchema.make("Dominic", 41,
                ImmutableMap.of(
                        "current", addressSchema.make(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"),
                        "previous", addressSchema.make(asList("63 Penguin Lane", "Walsall"), "RA8 81T")));

        assertThat(mapper.writeValueAsString(person), equalTo(
            "{\"_tupleType\":\"person\"," +
                "\"name\":\"Dominic\"," +
                "\"age\":41," +
                "\"addresses\":{" +
                "\"current\":{\"_tupleType\":\"address\"," +
                "\"addressLines\":[\"23 Acacia Avenue\",\"Sunderland\"]," +
                "\"postcode\":\"VB6 5UX\"}," +
                "\"previous\":{\"_tupleType\":\"address\"," +
                "\"addressLines\":[\"63 Penguin Lane\",\"Walsall\"]," +
                "\"postcode\":\"RA8 81T\"}}}"));
    }

}
