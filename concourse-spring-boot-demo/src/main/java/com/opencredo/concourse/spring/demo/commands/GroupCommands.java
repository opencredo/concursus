package com.opencredo.concourse.spring.demo.commands;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesCommandsFor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@HandlesCommandsFor("group")
public interface GroupCommands {

    CompletableFuture<UUID> create(StreamTimestamp ts, UUID groupId, String groupName);
    CompletableFuture<Void> delete(StreamTimestamp ts, UUID groupId);

}
