package com.opencredo.concourse.domain.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concourse.domain.commands.CommandTypeMatcher;
import com.opencredo.concourse.domain.commands.channels.CommandInChannel;
import com.opencredo.concourse.domain.commands.channels.CommandOutChannel;
import com.opencredo.concourse.domain.commands.dispatching.*;
import com.opencredo.concourse.domain.json.commands.channels.JsonCommandInChannel;
import com.opencredo.concourse.domain.json.commands.channels.JsonCommandOutChannel;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesCommandsFor;
import com.opencredo.concourse.mapping.commands.methods.dispatching.MethodDispatchingCommandProcessor;
import com.opencredo.concourse.mapping.commands.methods.proxying.CommandProxyFactory;
import com.opencredo.concourse.mapping.commands.methods.reflection.CommandInterfaceInfo;
import org.junit.Test;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class JsonCommandOutChannelTest {

    @HandlesCommandsFor("test")
    public interface TestCommands {
        CompletableFuture<UUID> create(StreamTimestamp ts, UUID aggregateId, String name);
    }

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final CommandTypeMatcher typeMatcher = CommandInterfaceInfo.forInterface(TestCommands.class).getCommandTypeMatcher();

    private final TestCommands commandProcessor = (ts, id, name) -> CompletableFuture.completedFuture(id);

    private final CommandBus commandBus = LoggingCommandBus.using(
            new Slf4jCommandLog(),
            SynchronousCommandExecutor.processingWith(
                    MethodDispatchingCommandProcessor.dispatchingTo(
                            DispatchingCommandProcessor.create())
                            .subscribe(TestCommands.class, commandProcessor)));

    private final CommandOutChannel channelToBus = CommandOutChannel.toBus(commandBus);
    private final CommandInChannel<String, String> jsonInChannel = JsonCommandInChannel.using(typeMatcher, objectMapper, channelToBus);
    private final CommandInChannel<String, String> loggingInChannel = jsonIn -> {
        System.out.println(jsonIn);
        return jsonInChannel.apply(jsonIn).thenApply(jsonOut -> {
            System.out.println(jsonOut);
            return jsonOut;
        });
    };

    private final CommandOutChannel jsonOutChannel = JsonCommandOutChannel.using(objectMapper, loggingInChannel);
    private final CommandProxyFactory proxyFactory = CommandProxyFactory.proxying(jsonOutChannel);
    private final TestCommands proxy = proxyFactory.getProxy(TestCommands.class);

    @Test
    public void dispatchViaJsonChannel() throws ExecutionException, InterruptedException {
        UUID aggregateId = UUID.randomUUID();

        assertThat(
                proxy.create(StreamTimestamp.of("test", Instant.now()), aggregateId, "Arthur Putey").get(),
                equalTo(aggregateId));
    }
}
