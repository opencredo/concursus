package com.opencredo.concourse.domain.commands.dispatching;

import com.opencredo.concourse.domain.commands.Command;
import com.opencredo.concourse.domain.commands.CommandResult;

import java.util.concurrent.CompletableFuture;

public final class LoggingCommandBus implements CommandBus {

    public static LoggingCommandBus using(CommandLog commandLog, CommandExecutor commandExecutor) {
        return new LoggingCommandBus(commandLog, commandExecutor);
    }

    private final CommandLog commandLog;
    private final CommandExecutor commandExecutor;

    private LoggingCommandBus(CommandLog commandLog, CommandExecutor commandExecutor) {
        this.commandLog = commandLog;
        this.commandExecutor = commandExecutor;
    }

    @Override
    public CompletableFuture<CommandResult> apply(Command command) {
        CompletableFuture<CommandResult> future = new CompletableFuture<>();
        future.thenAccept(commandLog::logCommandResult);

        try {
            Command loggedCommand = commandLog.logCommand(command);
            commandExecutor.accept(loggedCommand, future);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }
}
