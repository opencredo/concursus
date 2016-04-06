package com.opencredo.concursus.hazelcast.commands;

import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandResult;
import com.opencredo.concursus.domain.commands.dispatching.CommandProcessor;
import com.opencredo.concursus.domain.time.TimeUUID;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Callable;

final class RemoteCommand implements Callable<CommandResult>, CommandProcessorAware {

    static RemoteCommand processing(Command command) {
        return new RemoteCommand(command);
    }

    private transient CommandProcessor commandProcessor;
    private final Command command;

    private RemoteCommand(Command command) {
        this.command = command;
    }

    @Override
    public CommandResult call() throws Exception {
        Command commandWithProcessingId = command.getProcessingId().isPresent()
                ? command
                : command.processed(TimeUUID.timeBased());

        try {
            Optional<Object> result = commandProcessor.process(commandWithProcessingId);
            return commandWithProcessingId.complete(Instant.now(), result);
        } catch (Exception e) {
            return commandWithProcessingId.fail(Instant.now(), e);
        }
    }

    @Override
    public void setCommandProcessor(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    public Command getCommand() {
        return command;
    }
}
