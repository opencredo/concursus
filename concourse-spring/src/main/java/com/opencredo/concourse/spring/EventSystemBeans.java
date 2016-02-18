package com.opencredo.concourse.spring;

import com.opencredo.concourse.domain.events.batching.SimpleEventBatch;
import com.opencredo.concourse.domain.events.caching.InMemoryEventStore;
import com.opencredo.concourse.domain.events.dispatching.EventBus;
import com.opencredo.concourse.domain.events.logging.EventLog;
import com.opencredo.concourse.domain.events.publishing.EventPublisher;
import com.opencredo.concourse.domain.events.publishing.SubscribableEventPublisher;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.events.writing.EventWriter;
import com.opencredo.concourse.domain.events.writing.PublishingEventWriter;
import com.opencredo.concourse.mapping.methods.DispatchingEventSourceFactory;
import com.opencredo.concourse.mapping.methods.ProxyingEventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class EventSystemBeans {

    private final InMemoryEventStore inMemoryEventStore = InMemoryEventStore.empty();

    @Bean
    public EventSource eventSource() {
        return inMemoryEventStore.getEventSource();
    }

    @Bean
    public EventLog eventLog() {
        return inMemoryEventStore;
    }

    @Bean
    public DispatchingEventSourceFactory dispatchingEventSourceFactory(EventSource eventSource) {
        return DispatchingEventSourceFactory.dispatching(eventSource);
    }

    @Bean
    public SubscribableEventPublisher eventPublisher() {
        return new SubscribableEventPublisher();
    }

    @Bean
    public EventWriter eventWriter(EventLog eventLog, EventPublisher eventPublisher) {
        return PublishingEventWriter.using(eventLog, eventPublisher);
    }

    @Bean
    public EventBus eventBus(EventWriter eventWriter, ComponentScanningEventBatchFilter eventBatchFilter) {
        return () -> eventBatchFilter.apply(SimpleEventBatch.writingTo(eventWriter));
    }

    @Bean
    public ProxyingEventBus proxyingEventBus(EventBus eventBus) {
        return ProxyingEventBus.proxying(eventBus);
    }
}
