package com.opencredo.concursus.examples;

import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.*;

import java.time.LocalDate;
import java.util.Optional;

@HandlesEventsFor("person")
public final class Person {

    @HandlesCommandsFor("person")
    public interface Commands {
        Person create(StreamTimestamp ts, String personId, String name, LocalDate dob);
        Person changeName(StreamTimestamp ts, String personId, String newName);
        Person moveToAddress(StreamTimestamp ts, String personId, String addressId);
        void delete(StreamTimestamp ts, String personId);
    }

    @HandlesEventsFor("person")
    public interface Events {
        @Initial
        void created(StreamTimestamp ts, String personId, String name, LocalDate dateOfBirth);
        void changedName(StreamTimestamp ts, String personId, String newName);
        void movedToAddress(StreamTimestamp ts, String personId, String addressId);
        @Terminal
        void deleted(StreamTimestamp ts, String personId);
    }

    @HandlesEvent
    public static Person created(String id, String name, LocalDate dateOfBirth) {
        return new Person(id, name, dateOfBirth, Optional.empty());
    }

    private final String id;
    private String name;
    private final LocalDate dateOfBirth;
    private Optional<String> currentAddressId;
    private boolean deleted = false;

    private Person(String id, String name, LocalDate dateOfBirth, Optional<String> currentAddressId) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.currentAddressId = currentAddressId;
    }

    public Optional<String> getCurrentAddressId() {
        return currentAddressId;
    }

    @HandlesEvent
    public void changedName(String newName) {
        name = newName;
    }

    @HandlesEvent
    public void movedToAddress(String addressId) {
        currentAddressId = Optional.of(addressId);
    }

    @HandlesEvent
    public void deleted() {
        deleted = true;
    }

    public String getId() {
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
