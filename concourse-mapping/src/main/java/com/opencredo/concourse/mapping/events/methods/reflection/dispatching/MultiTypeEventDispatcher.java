package com.opencredo.concourse.mapping.events.methods.reflection.dispatching;

import com.opencredo.concourse.domain.events.EventType;

import java.util.Set;

/**
 * An EventDispatcher that dispatches events of multiple types to a target object.
 * @param <T> The type of the target object.
 */
public interface MultiTypeEventDispatcher<T> extends EventDispatcher<T> {

    /**
     * Get the {@link EventType}s that this event dispatcher knows how to handle.
     * @return
     */
    Set<EventType> getHandledEventTypes();

}
