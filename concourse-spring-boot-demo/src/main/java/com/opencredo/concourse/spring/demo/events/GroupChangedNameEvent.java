package com.opencredo.concourse.spring.demo.events;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;

import java.util.UUID;

@HandlesEventsFor("group")
public interface GroupChangedNameEvent {

    void changedName(StreamTimestamp ts, UUID groupId, String newName);

}
