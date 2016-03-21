package com.opencredo.concourse.domain.commands;

import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.domain.commands.dispatching.CommandBus;
import com.opencredo.concourse.domain.commands.dispatching.CommandExecutor;
import com.opencredo.concourse.domain.commands.dispatching.CommandLog;
import com.opencredo.concourse.domain.commands.dispatching.LoggingCommandBus;
import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.common.VersionedName;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class LoggingCommandBusTest {

    private final List<Command> loggedCommands = new ArrayList<>();
    private final List<CommandResult> loggedResults = new ArrayList<>();
    private final TupleSchema emptySchema = TupleSchema.of("empty");
    private final CommandLog commandLog = CommandLog.loggingTo(loggedCommands::add, loggedResults::add);

    @Test
    public void logsCommandRequestAndResult() throws ExecutionException, InterruptedException {
        Instant completionTime = Instant.now();
        CommandExecutor commandExecutor = (command, future) ->
                future.complete(command.complete(completionTime, Optional.of("OK")));

        CommandBus commandBus = LoggingCommandBus.using(commandLog, commandExecutor);

        final Command command = Command.of(
                AggregateId.of("test", UUID.randomUUID()),
                StreamTimestamp.of("test", Instant.now()),
                VersionedName.of("create", "0"),
                emptySchema.makeWith(),
                String.class
        );
        Optional<Object> result = commandBus.apply(command).get().getResultValue();

        assertThat(result, equalTo(Optional.of("OK")));
        assertThat(getLoggedCommands(), contains(command));
        assertThat(loggedResults.get(0), equalTo(loggedCommands.get(0).complete(completionTime, Optional.of("OK"))));
    }

    @Test
    public void logsCommandFailure() throws ExecutionException, InterruptedException {
        CommandExecutor commandExecutor = (command, future) ->
                future.complete(command.fail(Instant.now(), new IllegalStateException("Out of cheese")));

        CommandBus commandBus = LoggingCommandBus.using(commandLog, commandExecutor);

        final Command command = Command.of(
                AggregateId.of("test", UUID.randomUUID()),
                StreamTimestamp.of("test", Instant.now()),
                VersionedName.of("create", "0"),
                emptySchema.makeWith(),
                String.class
        );
        Exception exception = commandBus.apply(command).get().getException();
        assertThat(exception.getMessage(), equalTo("Out of cheese"));

        assertThat(getLoggedCommands(), contains(command));
        assertThat(loggedResults.get(0).getException().getMessage(), equalTo("Out of cheese"));
    }

    @Test
    public void doesNotLogCommandExecutionFailure() throws ExecutionException, InterruptedException {
        CommandExecutor commandExecutor = (command, future) -> future.cancel(true);

        CommandBus commandBus = LoggingCommandBus.using(commandLog, commandExecutor);

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
