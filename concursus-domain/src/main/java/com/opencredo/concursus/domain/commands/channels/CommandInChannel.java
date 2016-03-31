package com.opencredo.concursus.domain.commands.channels;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Channel through which Commands are received into the system, e.g. as JSON from a message queue.
 * @param <I> The type of the inbound value, which is converted into a Command
 * @param <O> The type of the outbound value, which is converted from an {@link Optional} result
 */
public interface CommandInChannel<I, O> extends Function<I, CompletableFuture<O>> {
}
