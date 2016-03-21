package com.opencredo.concourse.spring.events.filtering.scanning;

import com.opencredo.concourse.domain.events.dispatching.EventBus;
import com.opencredo.concourse.domain.events.filtering.bus.EventBusFilter;
import com.opencredo.concourse.spring.events.filtering.FilterOrdering;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ComponentScanningEventBusFilter implements ApplicationContextAware, EventBusFilter {

    private EventBusFilter composedFilter;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        composedFilter = EventBusFilter.compose(
                applicationContext.getBeansOfType(EventBusFilter.class).values().stream()
                .filter(filter -> filter != this)
                .sorted(FilterOrdering.filterOrderComparator)
                .toArray(EventBusFilter[]::new));
    }

    @Override
    public EventBus apply(EventBus eventBus) {
        return composedFilter.apply(eventBus);
    }
}
