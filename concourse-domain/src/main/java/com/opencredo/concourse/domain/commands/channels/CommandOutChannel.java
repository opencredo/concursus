package com.opencredo.concourse.domain.commands.channels;

import com.opencredo.concourse.domain.commands.Command;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Channel through which commands are sent out of the system, e.g. to a CommandBus.
 */
public interface CommandOutChannel extends Function<Command, CompletableFuture<Optional<Object>>> {
}
