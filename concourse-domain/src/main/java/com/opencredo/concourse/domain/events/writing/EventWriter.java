package com.opencredo.concourse.domain.events.writing;

import com.opencredo.concourse.domain.events.Event;

import java.util.Collection;
import java.util.function.Consumer;

public interface EventWriter extends Consumer<Collection<Event>> {
}
