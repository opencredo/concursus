package com.opencredo.concursus.domain.commands.filters;

import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandResult;
import com.opencredo.concursus.domain.commands.dispatching.CommandExecutor;
import com.opencredo.concursus.domain.commands.dispatching.CommandLog;

import java.util.concurrent.CompletableFuture;

public final class LoggingCommandExecutorFilter implements CommandExecutorFilter {

    public static LoggingCommandExecutorFilter using(CommandLog commandLog) {
        return new LoggingCommandExecutorFilter(commandLog);
    }

    private final CommandLog commandLog;

    private LoggingCommandExecutorFilter(CommandLog commandLog) {
        this.commandLog = commandLog;
    }

    @Override
    public void onAccept(CommandExecutor commandExecutor, Command command, CompletableFuture<CommandResult> future) {
        future.thenAccept(commandLog::logCommandResult);

        try {
            Command loggedCommand = commandLog.logCommand(command);
            commandExecutor.accept(loggedCommand, future);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
    }
}
