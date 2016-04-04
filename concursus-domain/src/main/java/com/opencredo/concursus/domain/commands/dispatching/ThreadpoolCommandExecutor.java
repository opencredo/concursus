package com.opencredo.concursus.domain.commands.dispatching;

import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link CommandExecutor} that executes commands using an {@link ExecutorService}
 */
public final class ThreadpoolCommandExecutor implements CommandExecutor {

    /**
     * Create a {@link CommandExecutor} that serialises all commands to a single thread.
     * @param commandProcessor The {@link CommandProcessor} to process commands with.
     * @return The constructed {@link CommandExecutor}
     */
    public static CommandExecutor singleThreaded(CommandProcessor commandProcessor) {
        return processingWith(Executors.newSingleThreadExecutor(), commandProcessor);
    }


    /**
     * Create a {@link CommandExecutor} that dispatches command execution to the supplied {@link ExecutorService}.
     * @param executorService The {@link ExecutorService} to dispatch command execution to.
     * @param commandProcessor The {@link CommandProcessor} to process commands with.
     * @return The constructed {@link CommandExecutor}
     */
    public static CommandExecutor processingWith(ExecutorService executorService, CommandProcessor commandProcessor) {
        checkNotNull(executorService, "executorService must not be null");
        checkNotNull(commandProcessor, "commandProcessor must not be null");

        return new ThreadpoolCommandExecutor(executorService, ProcessingCommandExecutor.processingWith(commandProcessor));
    }

    private final ExecutorService executorService;
    private final CommandExecutor commandExecutor;

    private ThreadpoolCommandExecutor(ExecutorService executorService, CommandExecutor commandExecutor) {
        this.executorService = executorService;
        this.commandExecutor = commandExecutor;
    }

    @Override
    public void accept(Command command, CompletableFuture<CommandResult> future) {
        executorService.execute(() -> {
            commandExecutor.accept(command, future);
        });
    }
}
