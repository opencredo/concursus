package com.opencredo.concourse.mapping.commands.methods;

import com.opencredo.concourse.domain.commands.channels.CommandOutChannel;
import com.opencredo.concourse.domain.commands.dispatching.*;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesCommandsFor;
import com.opencredo.concourse.mapping.commands.methods.dispatching.MethodDispatchingCommandProcessor;
import com.opencredo.concourse.mapping.commands.methods.proxying.CommandProxyFactory;
import org.junit.Test;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.fail;

public class MethodInvokingCommandDispatcherTest {

    private final DispatchingCommandProcessor dispatchingCommandProcessor = DispatchingCommandProcessor.create();
    private final MethodDispatchingCommandProcessor dispatchingProcessor = MethodDispatchingCommandProcessor.dispatchingTo(dispatchingCommandProcessor);

    private final CommandBus commandBus = LoggingCommandBus.using(
            new Slf4jCommandLog(),
            SynchronousCommandExecutor.processingWith(dispatchingCommandProcessor));

    private final CommandProxyFactory commandProxyFactory = CommandProxyFactory.proxying(CommandOutChannel.toBus(commandBus));

    @HandlesCommandsFor("person")
    public interface PersonCommands {
        CompletableFuture<String> create(StreamTimestamp timestamp, UUID personId, String name);
    }

    @Test
    public void proxiesCommandMethods() throws ExecutionException, InterruptedException {
        dispatchingProcessor.subscribe(PersonCommands.class, (ts, personId, name) -> CompletableFuture.completedFuture("OK"));

        assertThat(commandProxyFactory.getProxy(PersonCommands.class).create(
                StreamTimestamp.of("test", Instant.now()),
                UUID.randomUUID(),
                "Arthur Putey").get(), equalTo("OK"));
    }

    @Test
    public void passesCompletableFutureFailureBackToClient() throws InterruptedException {
        dispatchingProcessor.subscribe(PersonCommands.class, (ts, personId, name) -> {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("Out of cheese"));
            return future;
        });

        CommandProxyFactory commandProxyFactory = CommandProxyFactory.proxying(CommandOutChannel.toBus(commandBus));

        CompletableFuture<String> result = commandProxyFactory.getProxy(PersonCommands.class).create(
                StreamTimestamp.of("test", Instant.now()),
                UUID.randomUUID(),
                "Arthur Putey");

        try {
            result.get();
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertThat(e.getCause(), instanceOf(IllegalStateException.class));
            assertThat(e.getCause().getMessage(), equalTo("Out of cheese"));
        }
    }

    @Test
    public void passesFailureBackToClient() throws InterruptedException {
        dispatchingProcessor.subscribe(PersonCommands.class, (ts, personId, name) -> {
            throw new IllegalStateException("Out of cheese");
        });

        CommandProxyFactory commandProxyFactory = CommandProxyFactory.proxying(CommandOutChannel.toBus(commandBus));

        CompletableFuture<String> result = commandProxyFactory.getProxy(PersonCommands.class).create(
                StreamTimestamp.of("test", Instant.now()),
                UUID.randomUUID(),
                "Arthur Putey");

        try {
            result.get();
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertThat(e.getCause(), instanceOf(IllegalStateException.class));
            assertThat(e.getCause().getMessage(), equalTo("Out of cheese"));
        }
    }
}
