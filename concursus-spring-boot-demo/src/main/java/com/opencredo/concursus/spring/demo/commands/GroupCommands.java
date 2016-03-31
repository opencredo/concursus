package com.opencredo.concursus.spring.demo.commands;

import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesCommandsFor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@HandlesCommandsFor("group")
public interface GroupCommands {

    CompletableFuture<UUID> create(StreamTimestamp ts, UUID groupId, String groupName);
    CompletableFuture<Void> delete(StreamTimestamp ts, UUID groupId);

}
