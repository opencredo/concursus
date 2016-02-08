package com.opencredo.concourse.data.tuples;

import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TupleSlotTest {

    @Test
    public void equality() {
        TupleSlot slot = TupleSlot.of("name", String.class);
        TupleSlot same = TupleSlot.of("name", String.class);
        TupleSlot differentName = TupleSlot.of("mane", String.class);
        TupleSlot differentType = TupleSlot.of("name", Integer.class);

        assertThat(slot, equalTo(same));
        assertThat(slot, not(equalTo(differentName)));
        assertThat(slot, not(equalTo(differentType)));
    }

    @Test
    public void hashing() {
        TupleSlot slot = TupleSlot.of("name", String.class);
        TupleSlot same = TupleSlot.of("name", String.class);
        TupleSlot differentName = TupleSlot.of("mane", String.class);
        TupleSlot differentType = TupleSlot.of("name", Integer.class);

        assertThat(slot.hashCode(), equalTo(same.hashCode()));
        assertThat(slot.hashCode(), not(equalTo(differentName.hashCode())));
        assertThat(slot.hashCode(), not(equalTo(differentType.hashCode())));
    }

    @Test
    public void stringRepresentation() {
        assertThat(TupleSlot.of("name", String.class).toString(), equalTo("name: java.lang.String"));
    }

    @Test
    public void testsValuesForConformity() {
        TupleSlot slot = TupleSlot.of("name", String.class);

        assertTrue(slot.accepts("name"));
        assertFalse(slot.accepts(4));
    }

    @Test
    public void testsOptionalValuesForConformity() {
        TupleSlot slot = TupleSlot.ofOptional("name", String.class);

        assertTrue(slot.accepts(Optional.empty()));
        assertTrue(slot.accepts(Optional.of("name")));
        assertFalse(slot.accepts(Optional.of(4)));
    }

    @Test
    public void testsListTypesForConformity() {
        TupleSlot slot = TupleSlot.ofList("things", String.class);

        assertTrue(slot.acceptsType(Types.listOf(String.class).getType()));
        assertFalse(slot.acceptsType(Types.listOf(Integer.class).getType()));
    }

    @Test
    public void testsMapTypesForConformity() {
        TupleSlot slot = TupleSlot.ofMap("things", String.class, Integer.class);

        assertTrue(slot.acceptsType(Types.mapOf(String.class, Integer.class).getType()));
        assertFalse(slot.acceptsType(Types.mapOf(String.class, String.class).getType()));
    }
}
