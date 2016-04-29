package com.opencredo.concursus.spring;

import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesCommandsFor;

@HandlesCommandsFor("person")
public interface PersonCommands {
    void create(StreamTimestamp ts, String personId, String name, int age);
    void updateNameAndAge(StreamTimestamp ts, String personId, String newName, int newAge);
}
