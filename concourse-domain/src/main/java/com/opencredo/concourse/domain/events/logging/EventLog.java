package com.opencredo.concourse.domain.events.logging;

import com.opencredo.concourse.domain.events.Event;

import java.util.Collection;
import java.util.function.UnaryOperator;

@FunctionalInterface
public interface EventLog extends UnaryOperator<Collection<Event>> {
}
