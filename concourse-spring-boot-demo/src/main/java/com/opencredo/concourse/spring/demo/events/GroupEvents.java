package com.opencredo.concourse.spring.demo.events;

import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;

@HandlesEventsFor("user")
public interface GroupEvents extends GroupCreatedEvent, GroupUpdatedEvents {

}
