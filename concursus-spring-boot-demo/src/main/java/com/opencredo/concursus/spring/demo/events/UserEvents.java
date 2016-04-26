package com.opencredo.concursus.spring.demo.events;

import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;
import com.opencredo.concursus.mapping.annotations.Initial;
import com.opencredo.concursus.mapping.annotations.Terminal;

@HandlesEventsFor("user")
public interface UserEvents {

    @Initial
    void created(StreamTimestamp ts, String userId, String userName, String passwordHash);
    void changedName(StreamTimestamp ts, String userId, String newName);
    void changedPassword(StreamTimestamp ts, String userId, String newPasswordHash);

    void addedToGroup(StreamTimestamp ts, String userId, String groupId);
    void removedFromGroup(StreamTimestamp ts, String userId, String groupId);

    @Terminal
    void deleted(StreamTimestamp ts, String userId);

}
