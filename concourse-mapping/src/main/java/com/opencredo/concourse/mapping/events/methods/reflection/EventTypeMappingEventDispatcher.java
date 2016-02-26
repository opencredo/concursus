package com.opencredo.concourse.mapping.events.methods.reflection;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

final class EventTypeMappingEventDispatcher<T> implements MultiEventDispatcher<T> {

    static <T> MultiEventDispatcher<T> mapping(Map<EventType, EventDispatcher<T>> eventDispatchers) {
        return new EventTypeMappingEventDispatcher<>(eventDispatchers);
    }

    private final Map<EventType, EventDispatcher<T>> eventDispatchers;

    private EventTypeMappingEventDispatcher(Map<EventType, EventDispatcher<T>> eventDispatchers) {
        this.eventDispatchers = eventDispatchers;
    }

    @Override
    public void accept(T target, Event event) {
        checkNotNull(event, "event must not be null");

        final EventDispatcher<T> dispatcher = eventDispatchers.get(EventType.of(event));
        checkState(dispatcher != null,
                "No dispatcher found for event " + event);

        dispatcher.accept(target, event);
    }

    @Override
    public Set<EventType> getHandledEventTypes() {
        return eventDispatchers.keySet();
    }
}
