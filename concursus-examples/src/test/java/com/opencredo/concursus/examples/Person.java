package com.opencredo.concursus.examples;

import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@HandlesEventsFor("person")
public final class Person {

    @HandlesCommandsFor("person")
    public interface Commands {
        CompletableFuture<Person> create(StreamTimestamp ts, UUID personId, String name, LocalDate dob);
        CompletableFuture<Person> changeName(StreamTimestamp ts, UUID personId, String newName);
        CompletableFuture<Person> moveToAddress(StreamTimestamp ts, UUID personId, UUID addressId);
        CompletableFuture<Void> delete(StreamTimestamp ts, UUID personId);
    }

    @HandlesEventsFor("person")
    public interface Events {
        @Initial
        void created(StreamTimestamp ts, UUID personId, String name, LocalDate dateOfBirth);
        void changedName(StreamTimestamp ts, UUID personId, String newName);
        void movedToAddress(StreamTimestamp ts, UUID personId, UUID addressId);
        @Terminal
        void deleted(StreamTimestamp ts, UUID personId);
    }

    @HandlesEvent
    public static Person created(UUID id, String name, LocalDate dateOfBirth) {
        return new Person(id, name, dateOfBirth, Optional.empty());
    }

    private final UUID id;
    private String name;
    private final LocalDate dateOfBirth;
    private Optional<UUID> currentAddressId;
    private boolean deleted = false;

    private Person(UUID id, String name, LocalDate dateOfBirth, Optional<UUID> currentAddressId) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.currentAddressId = currentAddressId;
    }

    public Optional<UUID> getCurrentAddressId() {
        return currentAddressId;
    }

    @HandlesEvent
    public void changedName(String newName) {
        name = newName;
    }

    @HandlesEvent
    public void movedToAddress(UUID addressId) {
        currentAddressId = Optional.of(addressId);
    }

    @HandlesEvent
    public void deleted() {
        deleted = true;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
