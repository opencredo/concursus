package com.opencredo.concourse.spring;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesCommandsFor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@HandlesCommandsFor("person")
public interface PersonCommands {
    CompletableFuture<Void> create(StreamTimestamp ts, UUID personId, String name, int age);
    CompletableFuture<Void> updateNameAndAge(StreamTimestamp ts, UUID personId, String newName, int newAge);
}
