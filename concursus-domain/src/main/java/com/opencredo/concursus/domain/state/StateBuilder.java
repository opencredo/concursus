package com.opencredo.concursus.domain.state;

import com.opencredo.concursus.domain.events.channels.EventOutChannel;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * An {@link EventOutChannel} which receives events and uses them to build a representation of an aggregate's state.
 * @param <T> The type of the state object to build.
 */
public interface StateBuilder<T> extends EventOutChannel, Supplier<Optional<T>> {

    /**
     * Get the constructed state instance, if present.
     * @return The constructed state instance, if present.
     */
    @Override
    public Optional<T> get();
}
