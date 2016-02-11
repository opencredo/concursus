package com.opencredo.concourse.domain.events.publishing;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public class SubscribableEventPublisher implements EventPublisher {

    private final ConcurrentMap<EventType, Consumer<Event>> subscribers = new ConcurrentHashMap<>();

    public SubscribableEventPublisher subscribe(EventType eventType, Consumer<Event> handler) {
        subscribers.compute(eventType, (key, value) -> value == null ? handler : value.andThen(handler));
        return this;
    }

    @Override
    public void accept(Event event) {
        EventType eventType = EventType.of(event);
        Consumer<Event> subscriber = subscribers.get(eventType);
        if (subscriber != null) {
            subscriber.accept(event);
        }
    }
}
