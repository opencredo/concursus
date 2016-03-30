package com.opencredo.concourse.examples;

import com.opencredo.concourse.domain.commands.dispatching.CommandBus;
import com.opencredo.concourse.domain.commands.dispatching.LoggingCommandBus;
import com.opencredo.concourse.domain.commands.dispatching.Slf4jCommandLog;
import com.opencredo.concourse.domain.commands.dispatching.ThreadpoolCommandExecutor;
import com.opencredo.concourse.domain.events.dispatching.EventBus;
import com.opencredo.concourse.domain.events.logging.EventLog;
import com.opencredo.concourse.domain.events.processing.EventBatchProcessor;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.state.StateRepository;
import com.opencredo.concourse.domain.storing.InMemoryEventStore;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.commands.methods.dispatching.CommandMethodDispatcher;
import com.opencredo.concourse.mapping.commands.methods.proxying.CommandProxyFactory;
import com.opencredo.concourse.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concourse.mapping.events.methods.state.DispatchingStateRepository;
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
