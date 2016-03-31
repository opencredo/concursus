package com.opencredo.concursus.domain.commands.dispatching;

import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandResult;
import com.opencredo.concursus.domain.time.TimeUUID;

import java.util.function.Consumer;

public interface CommandLog {

    static CommandLog loggingTo(Consumer<Command> commandLogger, Consumer<CommandResult> resultLogger) {
        return new CommandLog() {
            @Override
            public Command logCommand(Command command) {
                Command loggedCommand = command.processed(TimeUUID.timeBased());
                commandLogger.accept(loggedCommand);
                return loggedCommand;
            }

            @Override
            public void logCommandResult(CommandResult commandResult) {
                resultLogger.accept(commandResult);
            }
        };
    }

    Command logCommand(Command command);
    void logCommandResult(CommandResult commandResult);
}
