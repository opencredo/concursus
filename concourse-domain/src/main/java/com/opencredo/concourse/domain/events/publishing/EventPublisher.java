package com.opencredo.concourse.domain.events.publishing;

import com.opencredo.concourse.domain.events.Event;

import java.util.function.Consumer;

@FunctionalInterface
public interface EventPublisher extends Consumer<Event> {

}
