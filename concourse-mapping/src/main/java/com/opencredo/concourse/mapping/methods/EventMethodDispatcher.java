package com.opencredo.concourse.mapping.methods;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class EventMethodDispatcher implements Consumer<Event> {

    public static EventMethodDispatcher toHandler(Object target) {
        return toHandler(target.getClass(), target);
    }

    public static EventMethodDispatcher toHandler(Class<?> handlerInterface, Object target) {
        checkNotNull(handlerInterface, "handlerInterface must not be null");
        checkNotNull(target, "target must not be null");

        return new EventMethodDispatcher(target, EventInterfaceReflection.getEventDispatchers(handlerInterface));
    }

    private final Object target;
    private final Map<EventType, BiConsumer<Object, Event>> eventMappers;

    private EventMethodDispatcher(Object target, Map<EventType, BiConsumer<Object, Event>> eventMappers) {
        this.target = target;
        this.eventMappers = eventMappers;
    }

    @Override
    public void accept(Event event) {
        checkNotNull(event, "event must not be null");

        final BiConsumer<Object, Event> methodDispatcher = eventMappers.get(EventType.of(event));
        checkState(methodDispatcher != null,
                "No method dispatcher found for event " + event);

        methodDispatcher.accept(target, event);
    }
}
