package com.opencredo.concourse.spring.events;

import com.opencredo.concourse.domain.events.batching.SimpleEventBatch;
import com.opencredo.concourse.domain.events.cataloguing.AggregateCatalogue;
import com.opencredo.concourse.domain.events.cataloguing.InMemoryAggregateCatalogue;
import com.opencredo.concourse.domain.events.dispatching.EventBus;
import com.opencredo.concourse.domain.events.logging.EventLog;
import com.opencredo.concourse.domain.events.publishing.EventPublisher;
import com.opencredo.concourse.domain.events.publishing.EventSubscribable;
import com.opencredo.concourse.domain.events.publishing.SubscribableEventPublisher;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.events.writing.EventWriter;
import com.opencredo.concourse.domain.events.writing.PublishingEventWriter;
import com.opencredo.concourse.mapping.events.methods.dispatching.DispatchingEventSourceFactory;
import com.opencredo.concourse.mapping.events.methods.dispatching.DispatchingSubscriber;
import com.opencredo.concourse.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concourse.spring.events.filtering.ComponentScanningEventBatchFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class EventSystemBeans {

    private final SubscribableEventPublisher subscribableEventPublisher = new SubscribableEventPublisher();

    @Bean
    public DispatchingEventSourceFactory dispatchingEventSourceFactory(EventSource eventSource) {
        return DispatchingEventSourceFactory.dispatching(eventSource);
    }

    @Bean
    public EventPublisher eventPublisher() {
        return subscribableEventPublisher;
    }

    @Bean
    public EventSubscribable subscribable() {
        return subscribableEventPublisher;
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

    @Bean
    public DispatchingSubscriber dispatchingSubscriber(EventSubscribable subscribable) {
        return DispatchingSubscriber.subscribingTo(subscribable);
    }
}
