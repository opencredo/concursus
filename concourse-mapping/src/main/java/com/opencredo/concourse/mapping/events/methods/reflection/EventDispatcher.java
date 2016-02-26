package com.opencredo.concourse.mapping.events.methods.reflection;

import com.opencredo.concourse.domain.events.Event;

import java.util.function.BiConsumer;

public interface EventDispatcher<T> extends BiConsumer<T, Event> {

}
