package com.opencredo.concursus.spring.events.filtering.scanning;

import com.opencredo.concursus.domain.events.filtering.publisher.EventPublisherFilter;
import com.opencredo.concursus.domain.events.publishing.EventPublisher;
import com.opencredo.concursus.spring.events.filtering.FilterOrdering;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ComponentScanningEventPublisherFilter implements ApplicationContextAware, EventPublisherFilter {

    private EventPublisherFilter composedFilter;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        composedFilter = EventPublisherFilter.compose(
                applicationContext.getBeansOfType(EventPublisherFilter.class).values().stream()
                .filter(filter -> filter != this)
                .sorted(FilterOrdering.filterOrderComparator)
                .toArray(EventPublisherFilter[]::new));
    }

    @Override
    public EventPublisher apply(EventPublisher eventPublisher) {
        return composedFilter.apply(eventPublisher);
    }
}
