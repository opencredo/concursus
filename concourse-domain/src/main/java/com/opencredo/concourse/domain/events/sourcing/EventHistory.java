package com.opencredo.concourse.domain.events.sourcing;

import com.opencredo.concourse.domain.events.Event;

import java.util.List;

public interface EventHistory {

    List<Event> getEvents();

}
