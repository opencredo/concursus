package com.opencredo.concourse.spring.demo.events;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;

import java.util.UUID;

@HandlesEventsFor("user")
public interface GroupUpdatedEvents extends GroupChangedNameEvent {

    void userAdded(StreamTimestamp ts, UUID groupId, UUID userId);
    void userRemoved(StreamTimestamp ts, UUID groupId, UUID userId);
    void deleted(StreamTimestamp ts, UUID groupId);

}
