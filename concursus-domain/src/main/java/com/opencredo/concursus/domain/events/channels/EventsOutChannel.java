package com.opencredo.concursus.domain.events.channels;

import com.opencredo.concursus.domain.events.Event;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

@FunctionalInterface
public interface EventsOutChannel extends Consumer<Collection<Event>> {

    default EventOutChannel toEventOutChannel() {
        return event -> accept(Collections.singletonList(event));
    }

}
