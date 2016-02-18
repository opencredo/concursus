package com.opencredo.concourse.spring;

import com.opencredo.concourse.domain.events.caching.InMemoryEventStore;
import com.opencredo.concourse.domain.events.logging.EventLog;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;

@Configurable
public class TestConfiguration {

    private final InMemoryEventStore inMemoryEventStore = InMemoryEventStore.empty();

    @Bean
    public EventSource eventSource() {
        return inMemoryEventStore.getEventSource();
    }

    @Bean
    public EventLog eventLog() {
        return inMemoryEventStore;
    }

}
