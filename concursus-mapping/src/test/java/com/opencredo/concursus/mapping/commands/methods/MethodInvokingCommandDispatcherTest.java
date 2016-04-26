package com.opencredo.concursus.mapping.commands.methods;

import com.opencredo.concursus.domain.commands.dispatching.CommandBus;
import com.opencredo.concursus.domain.commands.dispatching.DispatchingCommandProcessor;
import com.opencredo.concursus.domain.commands.dispatching.ProcessingCommandExecutor;
import com.opencredo.concursus.domain.commands.dispatching.Slf4jCommandLog;
import com.opencredo.concursus.domain.commands.filters.LoggingCommandExecutorFilter;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesCommandsFor;
import com.opencredo.concursus.mapping.commands.methods.dispatching.MethodDispatchingCommandProcessor;
import com.opencredo.concursus.mapping.commands.methods.proxying.CommandExecutionException;
import com.opencredo.concursus.mapping.commands.methods.proxying.CommandProxyFactory;
import org.junit.Test;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.fail;

public class MethodInvokingCommandDispatcherTest {

    private final DispatchingCommandProcessor dispatchingCommandProcessor = DispatchingCommandProcessor.create();
    private final MethodDispatchingCommandProcessor dispatchingProcessor = MethodDispatchingCommandProcessor.dispatchingTo(dispatchingCommandProcessor);

    private final CommandBus commandBus = CommandBus.executingWith(
        LoggingCommandExecutorFilter.using(new Slf4jCommandLog()).apply(
            ProcessingCommandExecutor.processingWith(dispatchingCommandProcessor)));

    private final CommandProxyFactory commandProxyFactory = CommandProxyFactory.proxying(commandBus.toCommandOutChannel());

    @HandlesCommandsFor("person")
    public interface PersonCommands {
        String create(StreamTimestamp timestamp, String personId, String name);
    }

    @Test
    public void proxiesCommandMethods() throws ExecutionException, InterruptedException {
        dispatchingProcessor.subscribe(PersonCommands.class, (ts, personId, name) -> "OK");

        assertThat(commandProxyFactory.getProxy(PersonCommands.class).create(
                StreamTimestamp.of("test", Instant.now()),
                UUID.randomUUID().toString(),
                "Arthur Putey"), equalTo("OK"));
    }

    @Test
    public void passesCompletableFutureFailureBackToClient() throws InterruptedException {
        dispatchingProcessor.subscribe(PersonCommands.class, (ts, personId, name) -> {
            throw new IllegalStateException("Out of cheese");
        });

        CommandProxyFactory commandProxyFactory = CommandProxyFactory.proxying(commandBus.toCommandOutChannel());


        try {
            commandProxyFactory.getProxy(PersonCommands.class).create(
                    StreamTimestamp.of("test", Instant.now()),
                    UUID.randomUUID().toString(),
                    "Arthur Putey");
            fail("Expected exception");
        } catch (CommandExecutionException e) {
            assertThat(e.getCause(), instanceOf(IllegalStateException.class));
            assertThat(e.getCause().getMessage(), equalTo("Out of cheese"));
        }
    }

    @Test
    public void passesFailureBackToClient() throws InterruptedException {
        dispatchingProcessor.subscribe(PersonCommands.class, (ts, personId, name) -> {
            throw new IllegalStateException("Out of cheese");
        });

        CommandProxyFactory commandProxyFactory = CommandProxyFactory.proxying(commandBus.toCommandOutChannel());

        try {
            String result = commandProxyFactory.getProxy(PersonCommands.class).create(
                    StreamTimestamp.of("test", Instant.now()),
                    UUID.randomUUID().toString(),
                    "Arthur Putey");
            fail("Expected exception");
        } catch (CommandExecutionException e) {
            assertThat(e.getCause(), instanceOf(IllegalStateException.class));
            assertThat(e.getCause().getMessage(), equalTo("Out of cheese"));
        }
    }
}
