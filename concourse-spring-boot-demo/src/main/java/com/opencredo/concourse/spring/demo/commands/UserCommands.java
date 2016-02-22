package com.opencredo.concourse.spring.demo.commands;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesCommandsFor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@HandlesCommandsFor("user")
public interface UserCommands {

    CompletableFuture<UUID> create(StreamTimestamp ts, UUID userId, String userName, byte[] passwordHash);
    CompletableFuture<Void> updateName(StreamTimestamp ts, UUID userId, String newName);
    CompletableFuture<Void> addToGroup(StreamTimestamp ts, UUID userId, UUID groupId);
    CompletableFuture<Void> removeFromGroup(StreamTimestamp ts, UUID userId, UUID groupId);
    CompletableFuture<Void> delete(StreamTimestamp ts, UUID userId);

}
