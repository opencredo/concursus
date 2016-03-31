package com.opencredo.concursus.domain.events.publishing;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.EventType;

import java.util.function.Consumer;

/**
 * A receiver of {@link Event}s that forwards them to subscribed handlers by {@link EventType}.
 */
@FunctionalInterface
public interface EventSubscribable {

    /**
     * Register a handler to handle {@link Event}s of the given {@link EventType}.
     * @param eventType The type of event handled by the handler.
     * @param handler The handler to register.
     * @return The {@link EventSubscribable}, for method chaining.
     */
    EventSubscribable subscribe(EventType eventType, Consumer<Event> handler);

}
