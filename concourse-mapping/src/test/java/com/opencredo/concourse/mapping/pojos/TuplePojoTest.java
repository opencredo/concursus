package com.opencredo.concourse.mapping.pojos;

import com.google.common.collect.ImmutableMap;
import com.opencredo.concourse.data.tuples.Tuple;
import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.data.tuples.TupleSlot;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TuplePojoTest {

    private final TupleSchema schema = TupleSchema.of(
            TupleSlot.of("name", String.class),
            TupleSlot.of("age", int.class),
            TupleSlot.ofMap("telephoneNumbers", String.class, String.class)
    );

    public interface Person {
        String getName();
        int getAge();
        Map<String, String> getTelephoneNumbers();
    }

    @Test
    public void mapsTuple() {
        Tuple tuple = schema.makeWith("Arthur Putey", 41, ImmutableMap.of("home", "07773 456789"));
        Person person = TuplePojo.wrapping(tuple, Person.class);

        assertThat(person.getName(), equalTo("Arthur Putey"));
        assertThat(person.getAge(), equalTo(41));
        assertThat(person.getTelephoneNumbers().get("home"), equalTo("07773 456789"));
    }

    @Test
    public void equality() {
        Tuple tuple1 = schema.makeWith("Arthur Putey", 41, ImmutableMap.of("home", "07773 456789"));
        Tuple tuple2 = schema.makeWith("Arthur Putey", 41, ImmutableMap.of("home", "07773 456789"));
        Person person1 = TuplePojo.wrapping(tuple1, Person.class);
        Person person2 = TuplePojo.wrapping(tuple2, Person.class);

        assertThat(person1, equalTo(person2));
    }
}
