package com.opencredo.concourse.mapping.events.methods.reflection;

import com.opencredo.concourse.domain.events.EventType;

import java.util.Set;

public interface MultiEventDispatcher<T> extends EventDispatcher<T> {

    Set<EventType> getHandledEventTypes();

}
