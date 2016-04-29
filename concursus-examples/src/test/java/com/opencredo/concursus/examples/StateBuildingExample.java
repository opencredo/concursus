package com.opencredo.concursus.examples;

import com.opencredo.concursus.domain.events.state.StateBuilder;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.events.methods.proxying.EventEmittingProxy;
import com.opencredo.concursus.mapping.events.methods.state.DispatchingStateBuilder;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class StateBuildingExample {

    @Test
    public void buildStateFromEvents() {
        // Create a StateBuilder, and a proxy that dispatches events to it.
        StateBuilder<Person> personStateBuilder = DispatchingStateBuilder.dispatchingTo(Person.class);
        Person.Events proxy = EventEmittingProxy.proxying(personStateBuilder, Person.Events.class);

        String personId = "person1";
        proxy.created(StreamTimestamp.now(), personId, "Arthur Putey", LocalDate.parse("1968-05-28"));
        proxy.changedName(StreamTimestamp.now(), personId, "Arthur Daley");

        Person person = personStateBuilder.get().orElseThrow(IllegalStateException::new);

        assertThat(person.getId(), equalTo(personId));
        assertThat(person.getDateOfBirth(), equalTo(LocalDate.parse("1968-05-28")));
        assertThat(person.getCurrentAddressId(), equalTo(Optional.empty()));
        assertThat(person.getName(), equalTo("Arthur Daley"));

        // Move in to an address
        String address1Id = "address1";
        proxy.movedToAddress(StreamTimestamp.now(), personId, address1Id);
        assertThat(person.getCurrentAddressId(), equalTo(Optional.of(address1Id)));

        // Change address
        String address2Id = "address2";
        proxy.movedToAddress(StreamTimestamp.now(), personId, address2Id);
        assertThat(person.getCurrentAddressId(), equalTo(Optional.of(address2Id)));
    }

}
