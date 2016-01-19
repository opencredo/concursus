package com.opencredo.concourse.data.tuples;

import org.junit.Test;

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
}
