package com.opencredo.concursus.domain.commands.dispatching;

import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandResult;
import com.opencredo.concursus.domain.time.TimeUUID;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link CommandExecutor} that uses a {@link CommandProcessor} to process commands.
 */
public final class ProcessingCommandExecutor implements CommandExecutor {

    /**
     * Create a {@link CommandExecutor} that uses the supplied {@link CommandProcessor} to process commands.
     * @param commandProcessor The {@link CommandProcessor} to use to process commands.
     * @return The constructed {@link CommandExecutor}.
     */
    public static CommandExecutor processingWith(CommandProcessor commandProcessor) {
        return new ProcessingCommandExecutor(commandProcessor);
    }

    private final CommandProcessor commandProcessor;

    private ProcessingCommandExecutor(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    /**
     * Assigns a processing id to the command (if it does not already have one) and passes it to the wrapped
     * {@link CommandProcessor} for processing.
     * @param command The {@link Command} to execute.
     * @param future The {@link CompletableFuture} result of executing the command.
     */
    @Override
    public void accept(Command command, CompletableFuture<CommandResult> future) {
        Command commandWithProcessingId = command.getProcessingId().isPresent()
                ? command
                : command.processed(TimeUUID.timeBased());
        try {
            Optional<Object> result = commandProcessor.process(commandWithProcessingId);
            future.complete(commandWithProcessingId.complete(Instant.now(), result));
        } catch (Exception e) {
            future.complete(commandWithProcessingId.fail(Instant.now(), e));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
