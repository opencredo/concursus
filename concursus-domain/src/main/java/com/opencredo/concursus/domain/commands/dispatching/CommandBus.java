package com.opencredo.concursus.domain.commands.dispatching;

import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandResult;
import com.opencredo.concursus.domain.commands.channels.CommandOutChannel;
import com.opencredo.concursus.domain.functional.UnsafeFunction;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Dispatches commands to a {@link CommandExecutor}.
 */
public interface CommandBus extends Function<Command, CompletableFuture<CommandResult>> {

    /**
     * Create a {@link CommandBus} that dispatches commands to the supplied {@link CommandExecutor}.
     * @param executor The {@link CommandExecutor} to dispatch commands to.
     * @return The constructed {@link CommandBus}.
     */
    static CommandBus executingWith(CommandExecutor executor) {
        return command -> {
            CompletableFuture<CommandResult> future = new CompletableFuture<>();
            executor.accept(command, future);
            return future;
        };
    }

    /**
     * Get a {@link CommandOutChannel} that sends commands to this {@link CommandBus}.
     * @return The constructed {@link CommandOutChannel}.
     */
    default CommandOutChannel toCommandOutChannel() {
        return command ->
                apply(command).thenApply(result ->
                        result.join(Function.identity(), UnsafeFunction.of(e -> { throw e; })));
    }
}
