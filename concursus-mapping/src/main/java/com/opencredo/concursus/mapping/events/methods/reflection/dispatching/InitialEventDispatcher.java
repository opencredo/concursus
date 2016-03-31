package com.opencredo.concursus.mapping.events.methods.reflection.dispatching;

import com.opencredo.concursus.domain.events.Event;

import java.util.function.Function;

/**
 * Given an Event, an InitialEventDispatcher knows how to obtain an instance of a state type S.
 * @param <S> The type of the state object to construct from the event.
 */
@FunctionalInterface
public interface InitialEventDispatcher<S> extends Function<Event, S> {
}
