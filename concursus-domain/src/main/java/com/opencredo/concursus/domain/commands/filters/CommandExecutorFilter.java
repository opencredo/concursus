package com.opencredo.concursus.domain.commands.filters;

import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandResult;
import com.opencredo.concursus.domain.commands.dispatching.CommandExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

public interface CommandExecutorFilter extends UnaryOperator<CommandExecutor> {

    default CommandExecutor apply(CommandExecutor executor) {
        return (command, future) -> onAccept(executor, command, future);
    }

    void onAccept(CommandExecutor executor, Command command, CompletableFuture<CommandResult> future);

}
