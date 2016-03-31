package com.opencredo.concursus.data.tuples;

import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

public class TupleKeyTest {

    private final TupleSchema personSchema = TupleSchema.of("person",
            TupleSlot.of("name", String.class),
            TupleSlot.of("age", Integer.class),
            TupleSlot.of("address", Tuple.class)
    );

    private final TupleSchema addressSchema = TupleSchema.of("address",
            TupleSlot.ofList("addressLines", String.class),
            TupleSlot.of("postcode", String.class)
    );

    private final TupleKey<String> name = personSchema.getKey("name", String.class);
    private final TupleKey<Integer> age = personSchema.getKey("age", Integer.class);
    private final TupleKey<Tuple> address = personSchema.getKey("address", Tuple.class);
    private final TupleKey<List<String>> addressLines = addressSchema.getListKey("addressLines", String.class);
    private final TupleKey<String> postcode = addressSchema.getKey("postcode", String.class);

    @Test(expected=IllegalArgumentException.class)
    public void elementTypeChecking() {
        addressSchema.getListKey("addressLines", Integer.class);
    }

    @Test
    public void keyBasedAccessToValues() {
        Tuple person = personSchema.makeWith("Dominic", 41,
                addressSchema.makeWith(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));

        assertThat(person.get(name), equalTo("Dominic"));
        assertThat(person.get(age), equalTo(41));
        assertThat(person.get(address).get(addressLines), contains("23 Acacia Avenue", "Sunderland"));
        assertThat(person.get(address).get(postcode), equalTo("VB6 5UX"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void keyIsRefusedIfItBelongsToDifferentSchema() {
        Tuple person = personSchema.makeWith("Dominic", 41,
                addressSchema.makeWith(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));

        person.get(postcode);
    }

    @Test
    public void buildWithKeyValues() {
        Tuple person = personSchema.make(
                name.of("Dominic"),
                age.of(41),
                address.of(addressSchema.make(
                        addressLines.of(asList("23 Acacia Avenue", "Sunderland")),
                        postcode.of("VB6 5UX"))));

        assertThat(person, equalTo(
                personSchema.makeWith("Dominic", 41,
                        addressSchema.makeWith(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"))
        ));
    }


    @Test
    public void keyValuesAreRefusedIfAnyBelongsToDifferentSchema() {
        try {
            personSchema.make(
                    name.of("Dominic"),
                    postcode.of("foo"));
            fail("Incorrect keys should not be accepted");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), equalTo("" +
                    "Keys [TupleKey<name>,TupleKey<postcode>] " +
                    "do not all belong to schema " +
                    personSchema));
        }
    }

    @Test
    public void keyValuesAreRefusedIfAnyAreMissing() {
        try {
            personSchema.make(
                    name.of("Dominic"),
                    age.of(31));
            fail("Incomplete keys should not be accepted");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), equalTo("" +
                    "Not all slots in " + personSchema +
                    " filled by provided keys " +
                    "[TupleKey<name>,TupleKey<age>]"));
        }
    }

    @Test
    public void equality() {
        assertThat(personSchema.getKey("name", String.class),
            equalTo(personSchema.getKey("name", String.class)));
    }

}
