package com.opencredo.concursus.spring.cassandra;

import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;

@HandlesEventsFor("person")
public interface PersonEvents {
    void created(StreamTimestamp timestamp, String personId, String name, int age);
    void updatedAge(StreamTimestamp timestamp, String personId, int newAge);
    void updatedName(StreamTimestamp timestamp, String personId, String newName);
}
