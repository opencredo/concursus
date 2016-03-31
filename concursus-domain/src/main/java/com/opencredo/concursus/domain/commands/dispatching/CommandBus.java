package com.opencredo.concursus.domain.commands.dispatching;

import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandResult;
import com.opencredo.concursus.domain.commands.channels.CommandOutChannel;
import com.opencredo.concursus.domain.functional.UnsafeFunction;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface CommandBus extends Function<Command, CompletableFuture<CommandResult>> {

    default CommandOutChannel toCommandOutChannel() {
        return command ->
                apply(command).thenApply(result ->
                        result.join(Function.identity(), UnsafeFunction.of(e -> { throw e; })));
    }
}
