package com.opencredo.concourse.domain.events.channels;

import com.opencredo.concourse.domain.events.Event;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A channel through which an encoded collection {@link Event}s can be passed into the system.
 * @param <I> The type of the value which encodes the collection of {@link Event}s.
 */
public interface EventsInChannel<I> extends Consumer<I> {

    /**
     * Receive an encoded collection of events, decode them and pass them into the system.
     * @param input The encoded collection of events.
     */
    @Override
    void accept(I input);

    /**
     * Create an {@link EventInChannel} which converts each single encoded {@link Event} into an encoded collection of
     * events, and passes the encoded collection to this channel.
     * @param collectionBuilder A {@link Function} which converts a single encoded event into an encoded collection of
     *                          events.
     * @param <E> The type of the value which encodes a single {@link Event}.
     * @return The constructed {@link EventInChannel}.
     */
    default <E> EventInChannel<E> toEventInChannel(Function<E, I> collectionBuilder) {
        return i -> accept(collectionBuilder.apply(i));
    }

}
