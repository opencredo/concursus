package com.opencredo.concourse.mapping.events.methods.reflection.dispatching;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public final class TypeMappingInitialEventDispatcher<T> implements InitialEventDispatcher<T> {

    public static <T> InitialEventDispatcher<T> mapping(Map<EventType, InitialEventDispatcher<T>> eventDispatchers) {
        return new TypeMappingInitialEventDispatcher<T>(eventDispatchers);
    }

    private final Map<EventType, InitialEventDispatcher<T>> eventDispatchers;

    private TypeMappingInitialEventDispatcher(Map<EventType, InitialEventDispatcher<T>> eventDispatchers) {
        this.eventDispatchers = eventDispatchers;
    }

    @Override
    public T apply(Event event) {
        InitialEventDispatcher<T> dispatcher = eventDispatchers.get(EventType.of(event));
        checkArgument(dispatcher != null,
                "No dispatcher found for initial event %s", event);

        return dispatcher.apply(event);
    }
}
