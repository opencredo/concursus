package com.opencredo.concursus.domain.commands.dispatching;

import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandResult;
import com.opencredo.concursus.domain.common.AggregateId;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A {@link CommandExecutor} that partitions command execution based on a hash of each command's aggregate id.
 */
public final class PartitioningCommandExecutor implements CommandExecutor {

    /**
     * Create a partitioning {@link CommandExecutor} that dispatches each partition's commands to a single-threaded
     * command executor, ensuring that no two commands for the same aggregateId are ever executed simultaneously
     * (by the current process - use a HazelcastPartitioningCommandExecutor to support this guarantee across a cluster).
     * @param commandProcessor The {@link CommandProcessor} to use to process commands.
     * @param partitionCount The number of partitions to create.
     * @return The constructed {@link CommandExecutor}
     */
    public static CommandExecutor processingWith(CommandProcessor commandProcessor, int partitionCount) {
        return partitioningBetween(IntStream.range(0, partitionCount)
                .mapToObj(i -> ThreadpoolCommandExecutor.singleThreaded(commandProcessor))
                .collect(Collectors.toList()));
    }


    /**
     * Create a partitioning {@link CommandExecutor} that distributes {@link Command}s among the supplied list of
     * {@link CommandExecutor}s by aggregateId, ensuring that commands for the same aggregate id are always executed
     * by the same executor.
     * @param executors  The {@link CommandExecutor}s to dispatch commands to.
     * @return The constructed {@link CommandExecutor}
     */
    public static CommandExecutor partitioningBetween(List<CommandExecutor> executors) {
        return new PartitioningCommandExecutor(
                pickPartition(executors.size()).andThen(executors::get));
    }

    private static Function<AggregateId, Integer> pickPartition(int partitionCount) {
        return aggregateId -> Math.abs(aggregateId.hashCode() % partitionCount);
    }

    private final Function<AggregateId, CommandExecutor> executorPicker;

    private PartitioningCommandExecutor(Function<AggregateId, CommandExecutor> executorPicker) {
        this.executorPicker = executorPicker;
    }

    @Override
    public void accept(Command command, CompletableFuture<CommandResult> future) {
        executorPicker.apply(command.getAggregateId()).accept(command, future);
    }

}
