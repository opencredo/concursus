package com.opencredo.concursus.examples;

import com.opencredo.concursus.domain.state.StateRepository;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class PersonCommandProcessor implements Person.Commands {

    private final ProxyingEventBus eventBus;
    private final StateRepository<Person> personStateRepository;

    public PersonCommandProcessor(ProxyingEventBus eventBus, StateRepository<Person> personStateRepository) {
        this.eventBus = eventBus;
        this.personStateRepository = personStateRepository;
    }

    @Override
    public CompletableFuture<Person> create(StreamTimestamp ts, UUID personId, String name, LocalDate dob) {
        Optional<Person> person = eventBus.creating(Person.class, bus -> {
            bus.dispatch(Person.Events.class, e -> e.created(ts, personId, name, dob));
        });

        return person.map(CompletableFuture::completedFuture).orElseThrow(IllegalStateException::new);
    }

    @Override
    public CompletableFuture<Person> changeName(StreamTimestamp ts, UUID personId, String newName) {
        Person person = personStateRepository.getState(personId).orElseThrow(IllegalArgumentException::new);

        eventBus.updating(person, bus -> {
            bus.dispatch(Person.Events.class, e -> e.changedName(ts, personId, newName));
        });

        return CompletableFuture.completedFuture(person);
    }

    @Override
    public CompletableFuture<Person> moveToAddress(StreamTimestamp ts, UUID personId, UUID addressId) {
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

        return CompletableFuture.completedFuture(person);
    }

    @Override
    public CompletableFuture<Void> delete(StreamTimestamp ts, UUID personId) {
        eventBus.dispatch(Person.Events.class, e -> e.deleted(ts, personId));

        return CompletableFuture.completedFuture(null);
    }
}
