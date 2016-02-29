package com.opencredo.concourse.spring.demo.events;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.annotations.Initial;
import com.opencredo.concourse.mapping.annotations.Terminal;

import java.util.UUID;

@HandlesEventsFor("group")
public interface GroupEvents {

    @Initial
    void created(StreamTimestamp ts, UUID groupId, String groupName);
    void changedName(StreamTimestamp ts, UUID groupId, String newName);

    void userAdded(StreamTimestamp ts, UUID groupId, UUID userId);
    void userRemoved(StreamTimestamp ts, UUID groupId, UUID userId);

    @Terminal
    void deleted(StreamTimestamp ts, UUID groupId);
}
