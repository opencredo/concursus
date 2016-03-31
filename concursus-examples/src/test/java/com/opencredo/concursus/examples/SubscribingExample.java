package com.opencredo.concursus.examples;

import com.opencredo.concursus.domain.commands.dispatching.CommandBus;
import com.opencredo.concursus.domain.commands.dispatching.LoggingCommandBus;
import com.opencredo.concursus.domain.commands.dispatching.Slf4jCommandLog;
import com.opencredo.concursus.domain.commands.dispatching.ThreadpoolCommandExecutor;
import com.opencredo.concursus.domain.events.dispatching.EventBus;
import com.opencredo.concursus.domain.events.logging.EventLog;
import com.opencredo.concursus.domain.events.processing.PublishingEventBatchProcessor;
import com.opencredo.concursus.domain.events.publishing.SubscribableEventPublisher;
import com.opencredo.concursus.domain.events.sourcing.EventSource;
import com.opencredo.concursus.domain.state.StateRepository;
import com.opencredo.concursus.domain.storing.InMemoryEventStore;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.commands.methods.dispatching.CommandMethodDispatcher;
import com.opencredo.concursus.mapping.commands.methods.proxying.CommandProxyFactory;
import com.opencredo.concursus.mapping.events.methods.dispatching.DispatchingSubscriber;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concursus.mapping.events.methods.state.DispatchingStateRepository;
import org.junit.Test;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class SubscribingExample {

    private final InMemoryEventStore eventStore = InMemoryEventStore.empty();
    private final EventSource eventSource = EventSource.retrievingWith(eventStore);

    private final SubscribableEventPublisher eventPublisher = new SubscribableEventPublisher();
    private final EventLog eventLog = EventLog.loggingTo(eventStore);

    private final ProxyingEventBus eventBus = ProxyingEventBus.proxying(
            EventBus.processingWith(PublishingEventBatchProcessor.using(eventLog, eventPublisher)));

    private final StateRepository<Person> personRepository = DispatchingStateRepository.using(
            eventSource, Person.class);
    private final Person.Commands commandProcessor = new PersonCommandProcessor(eventBus, personRepository);

    private final CommandBus commandBus = LoggingCommandBus.using(new Slf4jCommandLog(),
            ThreadpoolCommandExecutor.processingWith(
                    Executors.newCachedThreadPool(),
                    CommandMethodDispatcher.toHandler(Person.Commands.class, commandProcessor)));

    private final CommandProxyFactory commandProxyFactory = CommandProxyFactory.proxying(commandBus.toCommandOutChannel());

    @Test
    public void eventsAreDispatchedToSubscribersByType() throws ExecutionException, InterruptedException {
        Person.Events personEventHandler = mock(Person.Events.class);
        Address.Events addressEventHandler = mock(Address.Events.class);

        DispatchingSubscriber dispatchingSubscriber = DispatchingSubscriber.subscribingTo(eventPublisher);
        dispatchingSubscriber.subscribe(Person.Events.class, personEventHandler);
        dispatchingSubscriber.subscribe(Address.Events.class, addressEventHandler);

        Person.Commands personCommands = commandProxyFactory.getProxy(Person.Commands.class);
        UUID personId = UUID.randomUUID();
        UUID address1Id = UUID.randomUUID();
        UUID address2Id = UUID.randomUUID();

        personCommands.create(StreamTimestamp.now(), personId, "Arthur Daley", LocalDate.parse("1968-05-28")).get();
        personCommands.moveToAddress(StreamTimestamp.now(), personId, address1Id).get();
        personCommands.moveToAddress(StreamTimestamp.now(), personId, address2Id).get();

        verify(personEventHandler).created(any(StreamTimestamp.class), eq(personId), eq("Arthur Daley"), any(LocalDate.class));
        verify(personEventHandler).movedToAddress(any(StreamTimestamp.class), eq(personId), eq(address1Id));
        verify(personEventHandler).movedToAddress(any(StreamTimestamp.class), eq(personId), eq(address2Id));
        verifyNoMoreInteractions(personEventHandler);

        verify(addressEventHandler).personMovedIn(any(StreamTimestamp.class), eq(address1Id), eq(personId));
        verify(addressEventHandler).personMovedOut(any(StreamTimestamp.class), eq(address1Id), eq(personId));
        verify(addressEventHandler).personMovedIn(any(StreamTimestamp.class), eq(address2Id), eq(personId));
        verifyZeroInteractions(addressEventHandler);
    }
}
