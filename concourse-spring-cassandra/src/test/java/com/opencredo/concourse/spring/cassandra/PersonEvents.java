package com.opencredo.concourse.spring.cassandra;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;

import java.util.UUID;

@HandlesEventsFor("person")
public interface PersonEvents {
    void created(StreamTimestamp timestamp, UUID personId, String name, int age);
    void updatedAge(StreamTimestamp timestamp, UUID personId, int newAge);
    void updatedName(StreamTimestamp timestamp, UUID personId, String newName);
}
