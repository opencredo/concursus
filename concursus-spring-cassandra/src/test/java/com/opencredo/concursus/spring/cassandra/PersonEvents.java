package com.opencredo.concursus.spring.cassandra;

import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;

import java.util.UUID;

@HandlesEventsFor("person")
public interface PersonEvents {
    void created(StreamTimestamp timestamp, UUID personId, String name, int age);
    void updatedAge(StreamTimestamp timestamp, UUID personId, int newAge);
    void updatedName(StreamTimestamp timestamp, UUID personId, String newName);
}
