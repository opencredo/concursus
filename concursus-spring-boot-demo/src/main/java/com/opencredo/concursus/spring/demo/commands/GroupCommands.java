package com.opencredo.concursus.spring.demo.commands;

import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesCommandsFor;

@HandlesCommandsFor("group")
public interface GroupCommands {

    String create(StreamTimestamp ts, String groupId, String groupName);
    void delete(StreamTimestamp ts, String groupId);

}
