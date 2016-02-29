package com.opencredo.concourse.spring.demo.events;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.annotations.Initial;
import com.opencredo.concourse.mapping.annotations.Terminal;

import java.util.UUID;

@HandlesEventsFor("user")
public interface UserEvents {

    @Initial
    void created(StreamTimestamp ts, UUID userId, String userName, String passwordHash);
    void changedName(StreamTimestamp ts, UUID userId, String newName);
    void changedPassword(StreamTimestamp ts, UUID userId, String newPasswordHash);

    void addedToGroup(StreamTimestamp ts, UUID userId, UUID groupId);
    void removedFromGroup(StreamTimestamp ts, UUID userId, UUID groupId);

    @Terminal
    void deleted(StreamTimestamp ts, UUID userId);

}
