package com.opencredo.concursus.domain.commands;

import com.opencredo.concursus.data.tuples.TupleSchema;
import com.opencredo.concursus.domain.commands.dispatching.CommandBus;
import com.opencredo.concursus.domain.commands.dispatching.CommandExecutor;
import com.opencredo.concursus.domain.commands.dispatching.CommandLog;
import com.opencredo.concursus.domain.commands.filters.LoggingCommandExecutorFilter;
import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.common.VersionedName;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class LoggingCommandExecutorTest {

    private final List<Command> loggedCommands = new ArrayList<>();
    private final List<CommandResult> loggedResults = new ArrayList<>();
    private final TupleSchema emptySchema = TupleSchema.of("empty");
    private final CommandLog commandLog = CommandLog.loggingTo(loggedCommands::add, loggedResults::add);

    @Test
    public void logsCommandRequestAndResult() throws ExecutionException, InterruptedException {
        Instant completionTime = Instant.now();
        CommandExecutor commandExecutor = (command, future) ->
                future.complete(command.complete(completionTime, Optional.of("OK")));

        CommandBus commandBus = CommandBus.executingWith(
                LoggingCommandExecutorFilter.using(commandLog).apply(commandExecutor));

        final Command command = Command.of(
                AggregateId.of("test", UUID.randomUUID()),
                StreamTimestamp.of("test", Instant.now()),
                VersionedName.of("create", "0"),
                emptySchema.makeWith(),
                String.class
        );
        Optional<Object> result = commandBus.apply(command).get().join(Function.identity(), r -> Optional.empty());

        assertThat(result, equalTo(Optional.of("OK")));
        assertThat(getLoggedCommands(), contains(command));
        assertThat(loggedResults.get(0), equalTo(loggedCommands.get(0).complete(completionTime, Optional.of("OK"))));
    }

    @Test
    public void logsCommandFailure() throws ExecutionException, InterruptedException {
        CommandExecutor commandExecutor = (command, future) ->
                future.complete(command.fail(Instant.now(), new IllegalStateException("Out of cheese")));

        CommandBus commandBus = CommandBus.executingWith(
                LoggingCommandExecutorFilter.using(commandLog).apply(commandExecutor));

        final Command command = Command.of(
                AggregateId.of("test", UUID.randomUUID()),
                StreamTimestamp.of("test", Instant.now()),
                VersionedName.of("create", "0"),
                emptySchema.makeWith(),
                String.class
        );
        String exceptionMessage = commandBus.apply(command).get().join(left -> null, Exception::getMessage);
        assertThat(exceptionMessage, equalTo("Out of cheese"));

        assertThat(getLoggedCommands(), contains(command));
        assertThat(loggedResults.get(0).join(left -> null, Exception::getMessage), equalTo("Out of cheese"));
    }

    @Test
    public void doesNotLogCommandExecutionFailure() throws ExecutionException, InterruptedException {
        CommandExecutor commandExecutor = (command, future) -> future.cancel(true);

        CommandBus commandBus = CommandBus.executingWith(
                LoggingCommandExecutorFilter.using(commandLog).apply(commandExecutor));

        final Command command = Command.of(
                AggregateId.of("test", UUID.randomUUID()),
                StreamTimestamp.of("test", Instant.now()),
                VersionedName.of("create", "0"),
                emptySchema.makeWith(),
                String.class
        );

        try {
            commandBus.apply(command).get();
            fail("Failure should be propagated up to caller");
        } catch (CancellationException e) {
        }

        assertThat(getLoggedCommands(), contains(command));
        assertThat(loggedResults, hasSize(0));
    }

    private List<Command> getLoggedCommands() {
        return loggedCommands.stream().map(command ->
                Command.of(
                        command.getAggregateId(),
                        command.getCommandTimestamp(),
                        command.getCommandName(),
                        command.getParameters(),
                        command.getResultType()))
                .collect(Collectors.toList());
    }
}
