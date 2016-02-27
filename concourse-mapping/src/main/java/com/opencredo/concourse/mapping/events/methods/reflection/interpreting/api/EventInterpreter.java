package com.opencredo.concourse.mapping.events.methods.reflection.interpreting.api;

import com.opencredo.concourse.domain.events.Event;

public interface EventInterpreter {

    Object[] mapEvent(Event event);
}
