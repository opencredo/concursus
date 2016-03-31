package com.opencredo.concursus.domain.commands.dispatching;

import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandResult;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class SynchronousCommandExecutor implements CommandExecutor {

    public static SynchronousCommandExecutor processingWith(CommandProcessor commandProcessor) {
        return new SynchronousCommandExecutor(commandProcessor);
    }

    private final CommandProcessor commandProcessor;

    private SynchronousCommandExecutor(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    @Override
    public void accept(Command command, CompletableFuture<CommandResult> future) {
        try {
            Optional<Object> result = commandProcessor.process(command);
            future.complete(command.complete(Instant.now(), result));
        } catch (Exception e) {
            future.complete(command.fail(Instant.now(), e));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
