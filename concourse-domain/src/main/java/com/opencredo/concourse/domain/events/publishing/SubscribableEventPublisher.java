package com.opencredo.concourse.domain.events.publishing;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * An {@link EventPublisher} that publishes events to handlers subscribed via the {@link EventSubscribable} interface.
 */
public final class SubscribableEventPublisher implements EventPublisher, EventSubscribable {

    private final ConcurrentMap<EventType, List<Consumer<Event>>> subscribers = new ConcurrentHashMap<>();

    @Override
    public SubscribableEventPublisher subscribe(EventType eventType, Consumer<Event> handler) {
        subscribers.computeIfAbsent(eventType, key -> new ArrayList<>()).add(handler);
        return this;
    }

    @Override
    public void accept(Event event) {
        EventType eventType = EventType.of(event);
        List<Consumer<Event>> eventSubscribers = subscribers.get(eventType);
        if (eventSubscribers != null) {
            eventSubscribers.forEach(subscriber -> subscriber.accept(event));
        }
    }
}
