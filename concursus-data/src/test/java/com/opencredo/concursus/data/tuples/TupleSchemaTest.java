package com.opencredo.concursus.data.tuples;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.function.BiFunction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;

public class TupleSchemaTest {

    private final TupleSchema coordinateSchema = TupleSchema.of("coordinate",
            TupleSlot.of("x", Integer.class),
            TupleSlot.of("y", Integer.class));

    @Test
    public void createsTuples() {
        Tuple coordinate = coordinateSchema.makeWith(12, 34);

        assertThat(coordinate.get("x"), equalTo(12));
        assertThat(coordinate.get("y"), equalTo(34));
    }

    @Test(expected=IllegalArgumentException.class)
    public void validatesValueCountWhenCreatingTuples() {
        coordinateSchema.makeWith(1, 2, 3);
    }

    @Test(expected=IllegalArgumentException.class)
    public void validatesValueTypesWhenCreatingTuples() {
        coordinateSchema.makeWith(1, 12.5);
    }

    @Test
    public void describesAllTypeMismatches() {
        try {
            coordinateSchema.makeWith("p", 12.5);
            fail("Should throw exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(),
                    equalTo("Slot (x: java.lang.Integer) does not accept value <p>, " +
                            "Slot (y: java.lang.Integer) does not accept value <12.5>"));
        }
    }

    @Test
    public void equality() {
        TupleSchema same = TupleSchema.of("coordinate",
                TupleSlot.of("x", Integer.class),
                TupleSlot.of("y", Integer.class));

        TupleSchema different = TupleSchema.of("coordinate",
                TupleSlot.of("x", Integer.class),
                TupleSlot.of("z", Integer.class));

        assertThat(coordinateSchema, equalTo(same));
        assertThat(coordinateSchema, not(equalTo(different)));
    }

    @Test
    public void hashing() {
        TupleSchema same = TupleSchema.of("coordinate",
                TupleSlot.of("x", Integer.class),
                TupleSlot.of("y", Integer.class));

        TupleSchema different = TupleSchema.of("coordinate",
                TupleSlot.of("x", Integer.class),
                TupleSlot.of("z", Integer.class));

        assertThat(coordinateSchema.hashCode(), equalTo(same.hashCode()));
        assertThat(coordinateSchema.hashCode(), not(equalTo(different.hashCode())));
    }

    @Test
    public void toStringRepresentation() {
        assertThat(coordinateSchema.toString(),
                equalTo("coordinate{x: java.lang.Integer,y: java.lang.Integer}"));
    }

    @Test
    public void fromMap() {
        assertThat(coordinateSchema.make(ImmutableMap.of("y", 34, "x", 12)),
                equalTo(coordinateSchema.makeWith(12, 34)));
    }

    @Test
    public void deserialise() {
        BiFunction<String, Type, Object> deserialiser = (s, t) -> Integer.parseInt(s);

        assertThat(coordinateSchema.deserialise(deserialiser, ImmutableMap.of(
                "y", "34",
                "x", "12")),
                equalTo(coordinateSchema.makeWith(12, 34)));
    }
}
