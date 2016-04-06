package com.opencredo.concursus.domain.events.channels;

import com.opencredo.concursus.domain.events.Event;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@FunctionalInterface
public interface EventsOutChannel extends Consumer<Collection<Event>> {

    default EventOutChannel toEventOutChannel() {
        return event -> accept(Collections.singletonList(event));
    }

    /**
     * Filter this channel with the supplied {@link Predicate}.
     * @param eventFilter The {@link Predicate} to use to filter this channel.
     * @return The filtered {@link EventsOutChannel}.
     */
    default EventsOutChannel filter(Predicate<Event> eventFilter) {
        return events -> accept(events.stream().filter(eventFilter).collect(Collectors.toList()));
    }

}
