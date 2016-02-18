package com.opencredo.concourse.domain.events.publishing;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;

import java.util.function.Consumer;

@FunctionalInterface
public interface Subscribable {

    Subscribable subscribe(EventType eventType, Consumer<Event> handler);

}
