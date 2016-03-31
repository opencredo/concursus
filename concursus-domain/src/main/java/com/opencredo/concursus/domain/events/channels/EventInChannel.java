package com.opencredo.concursus.domain.events.channels;

import com.opencredo.concursus.domain.events.Event;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A channel through which an encoded {@link Event} can be passed into the system.
 * @param <I> The type of the value which encodes the {@link Event}.
 */
@FunctionalInterface
public interface EventInChannel<I> extends Consumer<I> {

    /**
     * Accept an encoded {@link Event}, decode it and pass it into the system for processing.
     * @param input The encoded {@link Event}.
     */
    @Override
    void accept(I input);

    /**
     * Create an {@link EventsInChannel} which receives an encoded collection of events, converts it into a collection
     * of encoded events, and then passes them one at a time into this channel.
     * @param collectionParser A {@link Function} which converts an encoded collection of events into a collection of
     *                         encoded events.
     * @param <E> The type of the encoded collection of events.
     * @return The constructed {@link EventsInChannel}.
     */
    default <E> EventsInChannel<E> toEventsInChannel(Function<E, Collection<I>> collectionParser) {
        return inputs -> collectionParser.apply(inputs).forEach(this::accept);
    }

    /**
     * Create an {@link EventsInChannel} which receives a collection of encoded events and passes them one at a time
     * into this channel
     * @return The constructed {@link EventsInChannel}.
     */
    default EventsInChannel<Collection<I>> toEventsInChannel() {
        return inputs -> inputs.forEach(this::accept);
    }

}
