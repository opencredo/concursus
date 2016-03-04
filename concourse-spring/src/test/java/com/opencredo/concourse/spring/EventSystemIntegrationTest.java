package com.opencredo.concourse.spring;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.commands.methods.proxying.ProxyingCommandBus;
import com.opencredo.concourse.mapping.events.methods.dispatching.DispatchingCachedEventSource;
import com.opencredo.concourse.mapping.events.methods.dispatching.DispatchingEventSourceFactory;
import com.opencredo.concourse.spring.commands.CommandSystemBeans;
import com.opencredo.concourse.spring.events.EventSystemBeans;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {
        EventSystemBeans.class,
        CommandSystemBeans.class,
        TestConfiguration.class
})
public class EventSystemIntegrationTest {

    @Autowired
    private ProxyingCommandBus proxyingCommandBus;

    @Autowired
    private DispatchingEventSourceFactory eventSourceDispatching;

    @Autowired
    private PersonEventHandler personEventHandler;

    @Test
    public void writeAndReadBatch() throws ExecutionException, InterruptedException {
        UUID personId1 = UUID.randomUUID();
        UUID personId2 = UUID.randomUUID();
        Instant start = Instant.now();

        final PersonCommands personCommands = proxyingCommandBus.getDispatcherFor(PersonCommands.class);

        personCommands.create(StreamTimestamp.of("test", start),
                personId1,
                "Arthur Putey",
                41).get();

        personCommands.updateNameAndAge(
                StreamTimestamp.of("test", start.plusMillis(1)),
                personId1,
                "Arthur Daley",
                42).get();

        personCommands.create(
                StreamTimestamp.of("test", start),
                personId2,
                "Arthur Dent",
                32);

        personCommands.updateNameAndAge(
                StreamTimestamp.of("test", start.plusMillis(1)),
                personId2,
                "Arthur Danto",
                32).get();

        final DispatchingCachedEventSource<PersonEvents> preloaded = eventSourceDispatching.to(PersonEvents.class)
                .preload(personId1, personId2);

        List<String> personHistory1 = preloaded.replaying(personId1).collectAll(eventSummariser());
        List<String> personHistory2 = preloaded.replaying(personId2).inAscendingOrder().collectAll(eventSummariser());

        assertThat(personHistory1, contains(
                "name was changed to Arthur Daley",
                "age was changed to 42",
                "Arthur Putey was created with age 41"
        ));

        assertThat(personHistory2, contains(
                "Arthur Dent was created with age 32",
                "age was changed to 32",
                "name was changed to Arthur Danto"
        ));

        System.out.println(personEventHandler.getPublishedEvents());
        assertThat(personEventHandler.getPublishedEvents(), contains(
                "Arthur Putey was created with age 41",
                "age was changed to 42",
                "name was changed to Arthur Daley",
                "Arthur Dent was created with age 32",
                "age was changed to 32",
                "name was changed to Arthur Danto"
        ));
    }

    private Function<Consumer<String>, PersonEvents> eventSummariser() {
        return caller -> new PersonEvents() {
            @Override
            public void created(StreamTimestamp timestamp, UUID personId, String name, int age) {
                caller.accept(name + " was created with age " + age);
            }

            @Override
            public void updatedAge(StreamTimestamp timestamp, UUID personId, int newAge) {
                caller.accept("age was changed to " + newAge);
            }

            @Override
            public void updatedName(StreamTimestamp timestamp, UUID personId, String newName) {
                caller.accept("name was changed to " + newName);
            }
        };
    }
}
