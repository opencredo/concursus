package com.opencredo.concourse.spring.events;

import com.opencredo.concourse.domain.events.batching.ProcessingEventBatch;
import com.opencredo.concourse.domain.events.dispatching.EventBus;
import com.opencredo.concourse.domain.events.logging.EventLog;
import com.opencredo.concourse.domain.events.processing.EventBatchProcessor;
import com.opencredo.concourse.domain.events.processing.PublishingEventBatchProcessor;
import com.opencredo.concourse.domain.events.publishing.EventPublisher;
import com.opencredo.concourse.domain.events.publishing.EventSubscribable;
import com.opencredo.concourse.domain.events.publishing.SubscribableEventPublisher;
import com.opencredo.concourse.domain.events.sourcing.EventRetriever;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.persisting.EventPersister;
import com.opencredo.concourse.domain.storing.EventStore;
import com.opencredo.concourse.domain.storing.InMemoryEventStore;
import com.opencredo.concourse.mapping.events.methods.dispatching.DispatchingEventSourceFactory;
import com.opencredo.concourse.mapping.events.methods.dispatching.DispatchingSubscriber;
import com.opencredo.concourse.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concourse.spring.events.filtering.scanning.ComponentScanningEventBatchFilter;
import com.opencredo.concourse.spring.events.filtering.scanning.ComponentScanningEventBusFilter;
import com.opencredo.concourse.spring.events.filtering.scanning.ComponentScanningEventLogFilter;
import com.opencredo.concourse.spring.events.filtering.scanning.ComponentScanningEventPublisherFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class EventSystemBeans {

    private final SubscribableEventPublisher subscribableEventPublisher = new SubscribableEventPublisher();

    @Bean
    public EventStore eventStore() {
        return InMemoryEventStore.empty();
    }

    @Bean
    public EventSource eventSource(EventRetriever eventRetriever) {
        return EventSource.retrievingWith(eventRetriever);
    }

    @Bean
    public EventLog eventLog(ComponentScanningEventLogFilter filter, EventPersister eventPersister) {
        return filter.apply(EventLog.loggingTo(eventPersister));
    }

    @Bean
    public DispatchingEventSourceFactory dispatchingEventSourceFactory(EventSource eventSource) {
        return DispatchingEventSourceFactory.dispatching(eventSource);
    }

    @Bean
    public EventSubscribable subscribable() {
        return subscribableEventPublisher;
    }

    @Bean
    public EventPublisher eventPublisher(ComponentScanningEventPublisherFilter filter) { return filter.apply(subscribableEventPublisher); }

    @Bean
    public EventBatchProcessor eventWriter(EventLog eventLog, EventPublisher eventPublisher) {
        return PublishingEventBatchProcessor.using(eventLog, eventPublisher);
    }

    @Bean
    public EventBus eventBus(EventBatchProcessor eventBatchProcessor, ComponentScanningEventBusFilter eventBusFilter, ComponentScanningEventBatchFilter eventBatchFilter) {
        return eventBusFilter.apply(() -> eventBatchFilter.apply(ProcessingEventBatch.processingWith(eventBatchProcessor)));
    }

    @Bean
    public ProxyingEventBus proxyingEventBus(EventBus eventBus) {
        return ProxyingEventBus.proxying(eventBus);
    }

    @Bean
    public DispatchingSubscriber dispatchingSubscriber(EventSubscribable subscribable) {
        return DispatchingSubscriber.subscribingTo(subscribable);
    }
}
