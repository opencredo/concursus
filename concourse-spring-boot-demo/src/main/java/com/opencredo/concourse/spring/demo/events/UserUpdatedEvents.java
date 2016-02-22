package com.opencredo.concourse.spring.demo.events;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;

import java.util.UUID;

@HandlesEventsFor("user")
public interface UserUpdatedEvents extends UserUpdatedPasswordEvent {

    void changedName(StreamTimestamp ts, UUID userId, String newName);
    void addedToGroup(StreamTimestamp ts, UUID userId, UUID groupId);
    void removedFromGroup(StreamTimestamp ts, UUID userId, UUID groupId);
    void deleted(StreamTimestamp ts, UUID userId);

}
