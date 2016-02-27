package com.opencredo.concourse.mapping.events.methods.reflection.dispatching;

import com.opencredo.concourse.domain.events.Event;

import java.util.function.BiConsumer;

/**
 * Given a target object of type T, and an Event, an EventDispatcher knows how to send the event to the target object.
 * @param <T> The type of the target object.
 */
public interface EventDispatcher<T> extends BiConsumer<T, Event> {
}
