package com.opencredo.concourse.domain.commands.dispatching;

import com.opencredo.concourse.domain.commands.Command;
import com.opencredo.concourse.domain.commands.CommandResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ThreadpoolCommandExecutor implements CommandExecutor {

    public static CommandExecutor singleThreaded(CommandProcessor commandProcessor) {
        return processingWith(Executors.newSingleThreadExecutor(), commandProcessor);
    }

    public static CommandExecutor processingWith(ExecutorService executorService, CommandProcessor commandProcessor) {
        return new ThreadpoolCommandExecutor(executorService, SynchronousCommandExecutor.processingWith(commandProcessor));
    }

    private final ExecutorService executorService;
    private final SynchronousCommandExecutor synchronousExecutor;

    private ThreadpoolCommandExecutor(ExecutorService executorService, SynchronousCommandExecutor synchronousExecutor) {
        this.executorService = executorService;
        this.synchronousExecutor = synchronousExecutor;
    }

    @Override
    public void accept(Command command, CompletableFuture<CommandResult> future) {
        executorService.execute(() -> {
            synchronousExecutor.accept(command, future);
        });
    }
}
