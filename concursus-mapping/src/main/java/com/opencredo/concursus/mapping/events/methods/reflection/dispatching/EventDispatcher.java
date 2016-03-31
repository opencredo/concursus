package com.opencredo.concursus.mapping.events.methods.reflection.dispatching;

import com.opencredo.concursus.domain.events.Event;

import java.util.function.BiConsumer;

/**
 * Given a target object of type T, and an Event, an EventDispatcher knows how to send the event to the target object.
 * @param <T> The type of the target object.
 */
public interface EventDispatcher<T> extends BiConsumer<T, Event> {
}
