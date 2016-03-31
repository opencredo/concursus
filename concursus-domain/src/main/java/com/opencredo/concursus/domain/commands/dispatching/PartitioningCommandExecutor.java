package com.opencredo.concursus.domain.commands.dispatching;

import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandResult;
import com.opencredo.concursus.domain.common.AggregateId;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class PartitioningCommandExecutor implements CommandExecutor {

    public static PartitioningCommandExecutor processingWith(CommandProcessor commandProcessor, int partitionCount) {
        return partitioningBetween(IntStream.range(0, partitionCount)
                .mapToObj(i -> ThreadpoolCommandExecutor.singleThreaded(commandProcessor))
                .collect(Collectors.toList()));
    }

    public static PartitioningCommandExecutor partitioningBetween(List<CommandExecutor> executors) {
        return new PartitioningCommandExecutor(
                pickPartition(executors.size()).andThen(executors::get));
    }

    private static Function<AggregateId, Integer> pickPartition(int partitionCount) {
        return aggregateId -> Math.abs(aggregateId.hashCode() % partitionCount);
    }

    private final Function<AggregateId, CommandExecutor> executorPicker;

    public PartitioningCommandExecutor(Function<AggregateId, CommandExecutor> executorPicker) {
        this.executorPicker = executorPicker;
    }

    @Override
    public void accept(Command command, CompletableFuture<CommandResult> future) {
        executorPicker.apply(command.getAggregateId()).accept(command, future);
    }

}
