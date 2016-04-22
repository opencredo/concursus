package com.opencredo.concursus.domain.json.commands.channels;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concursus.domain.commands.CommandTypeMatcher;
import com.opencredo.concursus.domain.commands.channels.CommandInChannel;
import com.opencredo.concursus.domain.commands.channels.CommandOutChannel;
import com.opencredo.concursus.domain.commands.dispatching.CommandBus;
import com.opencredo.concursus.domain.commands.dispatching.ProcessingCommandExecutor;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesCommandsFor;
import com.opencredo.concursus.mapping.commands.methods.dispatching.MethodDispatchingCommandProcessor;
import com.opencredo.concursus.mapping.commands.methods.proxying.CommandProxyFactory;
import com.opencredo.concursus.mapping.commands.methods.reflection.CommandInterfaceInfo;
import org.junit.Test;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class JsonCommandOutChannelTest {

    @HandlesCommandsFor("test")
    public interface TestCommands {
        UUID create(StreamTimestamp ts, UUID aggregateId, String name);
    }

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final CommandTypeMatcher typeMatcher = CommandInterfaceInfo.forInterface(TestCommands.class).getCommandTypeMatcher();

    private final TestCommands commandProcessor = (ts, id, name) -> id;

    private final CommandBus commandBus = CommandBus.executingWith(
        ProcessingCommandExecutor.processingWith(
                MethodDispatchingCommandProcessor.create()
                        .subscribe(TestCommands.class, commandProcessor)));

    private final CommandOutChannel channelToBus = commandBus.toCommandOutChannel();
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
    public void dispatchViaJsonChannel() {
        UUID aggregateId = UUID.randomUUID();

        assertThat(
                proxy.create(StreamTimestamp.of("test", Instant.now()), aggregateId, "Arthur Putey"),
                equalTo(aggregateId));
    }
}
