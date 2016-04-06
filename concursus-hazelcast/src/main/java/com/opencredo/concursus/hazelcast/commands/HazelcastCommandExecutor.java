package com.opencredo.concursus.hazelcast.commands;

import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.core.IExecutorService;
import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandResult;
import com.opencredo.concursus.domain.commands.dispatching.CommandExecutor;

import java.util.concurrent.CompletableFuture;

/**
 * A {@link CommandExecutor} that dispatches commands to Hazelcast nodes via an {@link IExecutorService} for execution,
 * partitioning by aggregate id.
 */
public final class HazelcastCommandExecutor implements CommandExecutor {

    /**
     * Create a new {@link HazelcastCommandExecutor} using the supplied {@link IExecutorService}.
     * @param executorService The {@link IExecutorService} to use to execute {@link Command}s.
     * @return The constructed {@link CommandExecutor}.
     */
    public static CommandExecutor using(IExecutorService executorService) {
        return new HazelcastCommandExecutor(executorService);
    }

    private final IExecutorService executorService;

    private HazelcastCommandExecutor(IExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void accept(Command command, CompletableFuture<CommandResult> commandResultCompletableFuture) {
        executorService.submitToKeyOwner(
                RemoteCommand.processing(command),
                command.getAggregateId().getId(),
                new ExecutionCallback<CommandResult>() {
            @Override
            public void onResponse(CommandResult commandResult) {
                commandResultCompletableFuture.complete(commandResult);
            }

            @Override
            public void onFailure(Throwable throwable) {
                commandResultCompletableFuture.completeExceptionally(throwable);
            }
        });
    }

}
