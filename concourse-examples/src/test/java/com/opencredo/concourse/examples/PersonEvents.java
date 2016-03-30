package com.opencredo.concourse.examples;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;

import java.time.LocalDate;
import java.util.UUID;

@HandlesEventsFor("person")
public interface PersonEvents {
    void created(StreamTimestamp ts, UUID personId, String name, LocalDate dateOfBirth);
    void changedName(StreamTimestamp ts, UUID personId, String newName);
    void addedToGroup(StreamTimestamp ts, UUID personId, UUID groupId);
    void removedFromGroup(StreamTimestamp ts, UUID personId, UUID groupId);
    void deleted(StreamTimestamp ts, UUID personId);
}
