package com.opencredo.concourse.spring.demo.events;

import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;

@HandlesEventsFor("user")
public interface UserEvents extends UserCreatedEvent, UserUpdatedEvents, UserChangedNameEvent {

}
