package com.opencredo.concourse.spring.demo.events;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;

import java.util.UUID;

@HandlesEventsFor("user")
public interface UserUpdatedPasswordEvent {

    void updatedPassword(StreamTimestamp ts, UUID userId, String newPasswordHash);

}
