package com.opencredo.concourse.mapping.events.methods.reflection.dispatching;

import com.opencredo.concourse.domain.events.Event;

import java.util.function.Function;

@FunctionalInterface
public interface InitialEventDispatcher<T> extends Function<Event, T> {
}
