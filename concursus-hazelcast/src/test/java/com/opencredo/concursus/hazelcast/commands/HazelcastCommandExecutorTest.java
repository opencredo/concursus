package com.opencredo.concursus.hazelcast.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.opencredo.concursus.domain.commands.dispatching.CommandBus;
import com.opencredo.concursus.domain.commands.dispatching.CommandExecutor;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesCommandsFor;
import com.opencredo.concursus.mapping.commands.methods.proxying.CommandIssuingProxy;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class HazelcastCommandExecutorTest {

    @HandlesCommandsFor("test")
    public interface TestCommands {
        CompletableFuture<String> uppercase(StreamTimestamp ts, UUID id, String input);
    }

    private final CommandExecutor executor = getCommandExecutor();

    private final CommandBus commandBus = CommandBus.executingWith(executor);
    private final TestCommands commandIssuingProxy = CommandIssuingProxy
            .proxying(commandBus.toCommandOutChannel(), TestCommands.class);

    private CommandExecutor getCommandExecutor() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        HazelcastCommandExecutorConfiguration config = HazelcastCommandExecutorConfiguration.using(objectMapper);
        config.subscribe(TestCommands.class, (ts, id, input) ->
                CompletableFuture.completedFuture(input.toUpperCase()));

        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(
                config.addCommandExecutorConfiguration(new Config()));

        return config.getCommandExecutor(hazelcastInstance);
    }

    @Test
    public void dispatchCommand() throws ExecutionException, InterruptedException {
        for (int i = 0; i < 100; i++) {
            assertEquals("FOO", commandIssuingProxy.uppercase(StreamTimestamp.now(), UUID.randomUUID(), "foo").get());
        }
    }

    @HandlesCommandsFor("test")
    public interface UnhandledCommands {
        CompletableFuture<String> unhandled(StreamTimestamp ts, UUID id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void commandFailsIfNotHandled() throws Throwable {
        try {
            CommandIssuingProxy.proxying(commandBus.toCommandOutChannel(), UnhandledCommands.class)
                    .unhandled(StreamTimestamp.now(), UUID.randomUUID()).get();
        } catch (ExecutionException e) {
            throw e.getCause().getCause();
        }
    }

}
