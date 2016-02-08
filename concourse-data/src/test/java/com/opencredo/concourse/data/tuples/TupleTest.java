package com.opencredo.concourse.data.tuples;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

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

    @Test
    public void resolvesNamesToValues() {
        Tuple person = personSchema.makeWith(
                "Dominic", 41,
                addressSchema.makeWith(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));

        assertThat(person.get("name"), equalTo("Dominic"));
        assertThat(person.get("age"), equalTo(41));
        assertThat(((Tuple) person.get("address")).get("postcode"), equalTo("VB6 5UX"));
    }

    @Test
    public void makeWithWithNamedValues() {
        Tuple person = personSchema.make(
                NamedValue.of("name", "Dominic"),
                NamedValue.of("age", 41),
                NamedValue.of("address", addressSchema.make(
                        NamedValue.of("addressLines", asList("23 Acacia Avenue", "Sunderland")),
                        NamedValue.of("postcode", "VB6 5UX"))));

        assertThat(person.get("name"), equalTo("Dominic"));
        assertThat(person.get("age"), equalTo(41));
        assertThat(((Tuple) person.get("address")).get("postcode"), equalTo("VB6 5UX"));
    }

    @Test
    public void toStringRepresentationContainsNameValuePairs() {
        Tuple person = personSchema.makeWith("Dominic", 41,
                addressSchema.makeWith(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));

        assertThat(person.toString(),
                equalTo("{name=Dominic, age=41, address={addressLines=[23 Acacia Avenue, Sunderland], postcode=VB6 5UX}}"));
    }

    @Test
    public void equality() {
        Tuple person = personSchema.makeWith("Dominic", 41,
                addressSchema.makeWith(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));
        Tuple same = personSchema.makeWith("Dominic", 41,
                addressSchema.makeWith(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));
        Tuple different = personSchema.makeWith("Dominic", 42,
                addressSchema.makeWith(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));

        assertThat(person, equalTo(same));
        assertThat(same, equalTo(person));
        assertThat(person, not(equalTo(different)));
    }

    @Test
    public void hashing() {
        Tuple person = personSchema.makeWith("Dominic", 41,
                addressSchema.makeWith(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));
        Tuple same = personSchema.makeWith("Dominic", 41,
                addressSchema.makeWith(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));
        Tuple different = personSchema.makeWith("Dominic", 42,
                addressSchema.makeWith(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));

        assertThat(person.hashCode(), equalTo(same.hashCode()));
        assertThat(person.hashCode(), not(equalTo(different.hashCode())));
    }

    @Test
    public void serialisation() {
        Tuple person = personSchema.makeWith("Dominic", 41,
                addressSchema.makeWith(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX"));

        assertThat(person.serialise(Object::toString), equalTo(ImmutableMap.of(
                "name", "Dominic",
                "age", "41",
                "address", "{addressLines=[23 Acacia Avenue, Sunderland], postcode=VB6 5UX}")));
    }

    @Test
    public void toMap() {
        final Tuple address = addressSchema.makeWith(asList("23 Acacia Avenue", "Sunderland"), "VB6 5UX");
        Tuple person = personSchema.makeWith("Dominic", 41,
                address);

        assertThat(person.toMap(), equalTo(ImmutableMap.of(
                "name", "Dominic",
                "age", 41,
                "address", address)));
    }
}
