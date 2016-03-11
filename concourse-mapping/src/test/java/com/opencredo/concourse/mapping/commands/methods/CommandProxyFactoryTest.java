package com.opencredo.concourse.mapping.commands.methods;

import com.opencredo.concourse.domain.commands.channels.CommandOutChannel;
import com.opencredo.concourse.domain.commands.dispatching.CommandBus;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.domain.time.TimeUUID;
import com.opencredo.concourse.mapping.annotations.HandlesCommandsFor;
import com.opencredo.concourse.mapping.commands.methods.proxying.CommandProxyFactory;
import org.junit.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

public class CommandProxyFactoryTest {

    @HandlesCommandsFor("person")
    public interface PersonCommands {
        CompletableFuture<String> create(StreamTimestamp timestamp, UUID personId, String name);
    }

    @Test
    public void proxiesCommandMethods() throws ExecutionException, InterruptedException {
        Instant timeCompleted = Instant.now();
        CommandBus commandBus = command -> CompletableFuture.completedFuture(
                command.processed(TimeUUID.timeBased()).complete(timeCompleted, Optional.of("OK")));

        CommandProxyFactory commandProxyFactory = CommandProxyFactory.proxying(CommandOutChannel.toBus(commandBus));

        assertThat(commandProxyFactory.getProxy(PersonCommands.class).create(
                StreamTimestamp.of("test", Instant.now()),
                UUID.randomUUID(),
                "Arthur Putey").get(), equalTo("OK"));
    }

    @Test
    public void passesFailureBackToClient() throws InterruptedException {
        Instant timeCompleted = Instant.now();
        CommandBus commandBus = command -> CompletableFuture.completedFuture(
                command.processed(TimeUUID.timeBased()).fail(
                        timeCompleted,
                        new IllegalStateException("Out of cheese")));

        CommandProxyFactory commandProxyFactory = CommandProxyFactory.proxying(CommandOutChannel.toBus(commandBus));

        CompletableFuture<String> result = commandProxyFactory.getProxy(PersonCommands.class).create(
                    StreamTimestamp.of("test", Instant.now()),
                    UUID.randomUUID(),
                    "Arthur Putey");

        try {
            result.get();
            fail("Expected exception");
        } catch (ExecutionException e) {
            e.printStackTrace();
            assertThat(e.getCause().getMessage(), equalTo("Out of cheese"));
        }
    }
}
