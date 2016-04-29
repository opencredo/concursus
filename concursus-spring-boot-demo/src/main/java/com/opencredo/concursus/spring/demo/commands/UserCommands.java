package com.opencredo.concursus.spring.demo.commands;

import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesCommandsFor;

@HandlesCommandsFor("user")
public interface UserCommands {

    String create(StreamTimestamp ts, String userId, String userName, byte[] passwordHash);
    void updateName(StreamTimestamp ts, String userId, String newName);
    void addToGroup(StreamTimestamp ts, String userId, String groupId);
    void removeFromGroup(StreamTimestamp ts, String userId, String groupId);
    void delete(StreamTimestamp ts, String userId);

}
