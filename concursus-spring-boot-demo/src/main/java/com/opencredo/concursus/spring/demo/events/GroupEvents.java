package com.opencredo.concursus.spring.demo.events;

import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;
import com.opencredo.concursus.mapping.annotations.Initial;
import com.opencredo.concursus.mapping.annotations.Terminal;

@HandlesEventsFor("group")
public interface GroupEvents {

    @Initial
    void created(StreamTimestamp ts, String groupId, String groupName);
    void changedName(StreamTimestamp ts, String groupId, String newName);

    void userAdded(StreamTimestamp ts, String groupId, String userId);
    void userRemoved(StreamTimestamp ts, String groupId, String userId);

    @Terminal
    void deleted(StreamTimestamp ts, String groupId);
}
