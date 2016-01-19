package com.opencredo.concourse.data.tuples;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TupleTest {

    private final TupleSchema personSchema = TupleSchema.of(
            TupleSlot.of("name", String.class),
            TupleSlot.of("age", Integer.class),
            TupleSlot.of("address", Tuple.class)
    );

    private final TupleSchema addressSchema = TupleSchema.of(
            TupleSlot.of("addressLines", new TypeToken<List<String>>() {}.getType()),
            TupleSlot.of("postcode", String.class)
    );

    private final TupleKey<String> name = personSchema.getKey("name", String.class);
    private final TupleKey<Integer> age = personSchema.getKey("age", Integer.class);
    private final TupleKey<Tuple> address = personSchema.getKey("address", Tuple.class);
    private final TupleKey<List<String>> addressLines = addressSchema.getListKey("addressLines", String.class);
    private final TupleKey<String> postcode = addressSchema.getKey("postcode", String.class);

    @Test
    public void resolvesNamesToValues() {
        Tuple person = personSchema.make(
                "Dominic", 41,
                addressSchema.make(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));

        assertThat(person.get("name"), equalTo("Dominic"));
        assertThat(person.get("age"), equalTo(41));
        assertThat(((Tuple) person.get("address")).get("postcode"), equalTo("VB6 5UX"));
    }

    @Test
    public void makeWithTupleBuilders() {
        Tuple person = personSchema.build(
                TupleBuilder.of("name", "Dominic"),
                TupleBuilder.of("age", 41),
                TupleBuilder.of("address", addressSchema.build(
                        TupleBuilder.of("addressLines", asList("23 Acacia Avenue", "Sunderland")),
                        TupleBuilder.of("postcode", "VB6 5UX"))));

        assertThat(person.get("name"), equalTo("Dominic"));
        assertThat(person.get("age"), equalTo(41));
        assertThat(((Tuple) person.get("address")).get("postcode"), equalTo("VB6 5UX"));
    }

    @Test
    public void keyBasedAccessToValues() {
        Tuple person = personSchema.make("Dominic", 41,
                addressSchema.make(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));

        assertThat(person.get(name), equalTo("Dominic"));
        assertThat(person.get(age), equalTo(41));
        assertThat(person.get(address).get(addressLines), contains("23 Acacia Avenue", "Sunderland"));
        assertThat(person.get(address).get(postcode), equalTo("VB6 5UX"));
    }

    @Test
    public void keyBasedTupleBuilding() {
        Tuple person = personSchema.build(
                name.of("Dominic"),
                age.of(41),
                address.of(addressSchema.build(
                        addressLines.of(asList("23 Acacia Avenue", "Sunderland")),
                        postcode.of("VB6 5UX"))));

        assertThat(person, equalTo(personSchema.make("Dominic", 41,
                addressSchema.make(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"))));
    }

    @Test(expected=IllegalArgumentException.class)
    public void keyIsRefusedIfItBelongsToDifferentSchema() {
        Tuple person = personSchema.make("Dominic", 41,
                addressSchema.make(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));

        TupleKey<String> postcode = addressSchema.getKey("postcode", String.class);

        person.get(postcode);
    }

    @Test
    public void toStringRepresentationContainsNameValuePairs() {
        Tuple person = personSchema.make("Dominic", 41,
                addressSchema.make(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));

        assertThat(person.toString(),
                equalTo("{name=Dominic, age=41, address={addressLines=[23 Acacia Avenue, Sunderland], postcode=VB6 5UX}}"));
    }

    @Test
    public void equality() {
        Tuple person = personSchema.make("Dominic", 41,
                addressSchema.make(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));
        Tuple same = personSchema.make("Dominic", 41,
                addressSchema.make(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));
        Tuple different = personSchema.make("Dominic", 42,
                addressSchema.make(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));

        assertThat(person, equalTo(same));
        assertThat(same, equalTo(person));
        assertThat(person, not(equalTo(different)));
    }

    @Test
    public void hashing() {
        Tuple person = personSchema.make("Dominic", 41,
                addressSchema.make(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));
        Tuple same = personSchema.make("Dominic", 41,
                addressSchema.make(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));
        Tuple different = personSchema.make("Dominic", 42,
                addressSchema.make(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));

        assertThat(person.hashCode(), equalTo(same.hashCode()));
        assertThat(person.hashCode(), not(equalTo(different.hashCode())));
    }

    @Test
    public void serialisation() {
        Tuple person = personSchema.make("Dominic", 41,
                addressSchema.make(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));

        assertThat(person.serialise(Object::toString), equalTo(ImmutableMap.of(
                "name", "Dominic",
                "age", "41",
                "address", "{addressLines=[23 Acacia Avenue, Sunderland], postcode=VB6 5UX}")));
    }

    @Test
    public void toMap() {
        final Tuple address = addressSchema.make(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX");
        Tuple person = personSchema.make("Dominic", 41,
                address);

        assertThat(person.toMap(), equalTo(ImmutableMap.of(
                "name", "Dominic",
                "age", 41,
                "address", address)));
    }
}
