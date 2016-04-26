package com.opencredo.concursus.examples;

import com.opencredo.concursus.domain.events.state.StateRepository;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class PersonCommandProcessor implements Person.Commands {

    private final ProxyingEventBus eventBus;
    private final StateRepository<Person> personStateRepository;

    public PersonCommandProcessor(ProxyingEventBus eventBus, StateRepository<Person> personStateRepository) {
        this.eventBus = eventBus;
        this.personStateRepository = personStateRepository;
    }

    @Override
    public Person create(StreamTimestamp ts, String personId, String name, LocalDate dob) {
        Optional<Person> person = eventBus.creating(Person.class, bus -> {
            bus.dispatch(Person.Events.class, e -> e.created(ts, personId, name, dob));
        });

        return person.orElseThrow(IllegalStateException::new);
    }

    @Override
    public Person changeName(StreamTimestamp ts, String personId, String newName) {
        Person person = personStateRepository.getState(personId).orElseThrow(IllegalArgumentException::new);

        eventBus.updating(person, bus -> {
            bus.dispatch(Person.Events.class, e -> e.changedName(ts, personId, newName));
        });

        return person;
    }

    @Override
    public Person moveToAddress(StreamTimestamp ts, String personId, String addressId) {
        Person person = personStateRepository.getState(personId).orElseThrow(IllegalArgumentException::new);

        eventBus.updating(personId, person, bus ->
                bus.dispatch(Person.Events.class, Address.Events.class, (p, a) ->
                    {
                        p.movedToAddress(ts, personId, addressId);
                        person.getCurrentAddressId().ifPresent(previousAddressId ->
                                a.personMovedOut(ts.subStream("out"), previousAddressId, personId));
                        a.personMovedIn(ts.subStream("in"), addressId, personId);
                    })
        );

        return person;
    }

    @Override
    public void delete(StreamTimestamp ts, String personId) {
        eventBus.dispatch(Person.Events.class, e -> e.deleted(ts, personId));
    }
}
