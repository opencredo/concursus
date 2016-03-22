package com.opencredo.concourse.domain.commands.dispatching;

import com.opencredo.concourse.domain.commands.Command;
import com.opencredo.concourse.domain.commands.CommandResult;
import com.opencredo.concourse.domain.commands.channels.CommandOutChannel;
import com.opencredo.concourse.domain.functional.UnsafeFunction;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface CommandBus extends Function<Command, CompletableFuture<CommandResult>> {

    default CommandOutChannel toCommandOutChannel() {
        return command ->
                apply(command).thenApply(result ->
                        result.join(Function.identity(), UnsafeFunction.of(e -> { throw e; })));
    }
}
