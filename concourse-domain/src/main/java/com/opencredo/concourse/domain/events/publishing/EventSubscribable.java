package com.opencredo.concourse.domain.events.publishing;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;

import java.util.function.Consumer;

@FunctionalInterface
public interface EventSubscribable {

    EventSubscribable subscribe(EventType eventType, Consumer<Event> handler);

}
