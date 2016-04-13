package com.opencredo.concursus.spring.events;

import com.opencredo.concursus.domain.events.batching.EventBatch;
import com.opencredo.concursus.domain.events.dispatching.EventBus;
import com.opencredo.concursus.domain.events.logging.EventLog;
import com.opencredo.concursus.domain.events.processing.EventBatchProcessor;
import com.opencredo.concursus.domain.events.processing.PublishingEventBatchProcessor;
import com.opencredo.concursus.domain.events.publishing.EventPublisher;
import com.opencredo.concursus.domain.events.publishing.EventSubscribable;
import com.opencredo.concursus.domain.events.publishing.SubscribableEventPublisher;
import com.opencredo.concursus.domain.events.sourcing.EventRetriever;
import com.opencredo.concursus.domain.events.sourcing.EventSource;
import com.opencredo.concursus.domain.events.persisting.EventPersister;
import com.opencredo.concursus.domain.events.storage.EventStore;
import com.opencredo.concursus.domain.events.storage.InMemoryEventStore;
import com.opencredo.concursus.mapping.events.methods.dispatching.DispatchingEventSourceFactory;
import com.opencredo.concursus.mapping.events.methods.dispatching.DispatchingSubscriber;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concursus.spring.events.filtering.scanning.ComponentScanningEventBatchFilter;
import com.opencredo.concursus.spring.events.filtering.scanning.ComponentScanningEventBusFilter;
import com.opencredo.concursus.spring.events.filtering.scanning.ComponentScanningEventLogFilter;
import com.opencredo.concursus.spring.events.filtering.scanning.ComponentScanningEventPublisherFilter;
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
        return eventBusFilter.apply(() -> eventBatchFilter.apply(EventBatch.processingWith(eventBatchProcessor)));
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
