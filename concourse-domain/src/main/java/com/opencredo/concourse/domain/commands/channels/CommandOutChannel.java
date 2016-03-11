package com.opencredo.concourse.domain.commands.channels;

import com.opencredo.concourse.domain.commands.Command;
import com.opencredo.concourse.domain.commands.dispatching.CommandBus;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface CommandOutChannel extends Function<Command, CompletableFuture<Optional<Object>>> {

    static CommandOutChannel toBus(CommandBus commandBus) {
        return commandBus::dispatchAsync;
    }

}
