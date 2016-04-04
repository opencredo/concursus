package com.opencredo.concursus.domain.commands.dispatching;

import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandResult;
import com.opencredo.concursus.domain.time.TimeUUID;

import java.util.function.Consumer;

/**
 * An object which knows how to log command requests and results.
 */
public interface CommandLog {

    /**
     * Create a command log using the supplied {@link Consumer}s to handle command requests and results.
     * @param requestLogger The {@link Consumer} to log command requests with.
     * @param resultLogger The {@link Consumer} to log command results with.
     * @return The constructed {@link CommandLog}.
     */
    static CommandLog loggingTo(Consumer<Command> requestLogger, Consumer<CommandResult> resultLogger) {
        return new CommandLog() {
            @Override
            public void logProcessedCommand(Command processedCommand) {
                requestLogger.accept(processedCommand);
            }

            @Override
            public void logCommandResult(CommandResult commandResult) {
                resultLogger.accept(commandResult);
            }
        };
    }

    /**
     * Log that the supplied {@link Command} was issued.
     * @param command The {@link Command} to log.
     * @return The logged {@link Command}, with a processing id attached.
     */
    default Command logCommand(Command command) {
        Command processedCommand = command.processed(TimeUUID.timeBased());
        logProcessedCommand(processedCommand);
        return processedCommand;
    }

    /**
     * Log that the supplied {@link Command} was issued.
     * @param processedCommand The {@link Command} to log.
     */
    void logProcessedCommand(Command processedCommand);

    /**
     * Log that the supplied {@link CommandResult} was returned.
     * @param commandResult The {@link CommandResult} to log.
     */
    void logCommandResult(CommandResult commandResult);
}
