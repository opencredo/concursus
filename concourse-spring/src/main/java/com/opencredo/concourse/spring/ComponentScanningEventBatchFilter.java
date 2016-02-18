package com.opencredo.concourse.spring;

import com.opencredo.concourse.domain.events.batching.EventBatch;
import com.opencredo.concourse.domain.events.filtering.EventBatchFilter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ComponentScanningEventBatchFilter implements ApplicationContextAware, EventBatchFilter {

    private EventBatchFilter composedFilter;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        composedFilter = EventBatchFilter.compose(
                applicationContext.getBeansOfType(EventBatchFilter.class).values().stream()
                .filter(filter -> filter != this)
                .sorted(FilterOrdering.filterOrderComparator)
                .toArray(EventBatchFilter[]::new));
    }

    @Override
    public EventBatch apply(EventBatch eventBatch) {
        return composedFilter.apply(eventBatch);
    }
}
