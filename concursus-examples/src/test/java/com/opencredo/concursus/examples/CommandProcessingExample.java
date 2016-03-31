package com.opencredo.concursus.examples;

import com.opencredo.concursus.domain.commands.dispatching.CommandBus;
import com.opencredo.concursus.domain.commands.dispatching.LoggingCommandBus;
import com.opencredo.concursus.domain.commands.dispatching.Slf4jCommandLog;
import com.opencredo.concursus.domain.commands.dispatching.ThreadpoolCommandExecutor;
import com.opencredo.concursus.domain.events.dispatching.EventBus;
import com.opencredo.concursus.domain.events.logging.EventLog;
import com.opencredo.concursus.domain.events.processing.EventBatchProcessor;
import com.opencredo.concursus.domain.events.sourcing.EventSource;
import com.opencredo.concursus.domain.state.StateRepository;
import com.opencredo.concursus.domain.storing.InMemoryEventStore;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.commands.methods.dispatching.CommandMethodDispatcher;
import com.opencredo.concursus.mapping.commands.methods.proxying.CommandProxyFactory;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concursus.mapping.events.methods.state.DispatchingStateRepository;
import org.junit.Test;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class CommandProcessingExample {

    private final InMemoryEventStore eventStore = InMemoryEventStore.empty();
    private final EventSource eventSource = EventSource.retrievingWith(eventStore);
    private final EventLog eventLog = EventLog.loggingTo(eventStore);

    private final ProxyingEventBus eventBus = ProxyingEventBus.proxying(
            EventBus.processingWith(EventBatchProcessor.loggingWith(eventLog)));

    private final StateRepository<Person> personRepository = DispatchingStateRepository.using(
            eventSource, Person.class);
    private final Person.Commands commandProcessor = new PersonCommandProcessor(eventBus, personRepository);

    private final CommandBus commandBus = LoggingCommandBus.using(new Slf4jCommandLog(),
            ThreadpoolCommandExecutor.processingWith(
                    Executors.newCachedThreadPool(),
                    CommandMethodDispatcher.toHandler(Person.Commands.class, commandProcessor)));

    private final CommandProxyFactory commandProxyFactory = CommandProxyFactory.proxying(commandBus.toCommandOutChannel());

    @Test
    public void issueCommands() throws ExecutionException, InterruptedException {
        Person.Commands personCommands = commandProxyFactory.getProxy(Person.Commands.class);

        Person createdPerson = personCommands.create(StreamTimestamp.now(), UUID.randomUUID(), "Arthur Putey", LocalDate.parse("1968-05-28")).get();

        assertThat(createdPerson.getName(), equalTo("Arthur Putey"));

        Person updatedPerson = personCommands.changeName(StreamTimestamp.now(), createdPerson.getId(), "Arthur Mumby").get();
        assertThat(updatedPerson.getName(), equalTo("Arthur Mumby"));

        Person movedPerson = personCommands.moveToAddress(StreamTimestamp.now(), createdPerson.getId(), UUID.randomUUID()).get();

        assertThat(movedPerson.getCurrentAddressId(), not(equalTo(updatedPerson.getCurrentAddressId())));
    }
}
