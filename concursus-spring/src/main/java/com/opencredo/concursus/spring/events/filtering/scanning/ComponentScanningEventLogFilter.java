package com.opencredo.concursus.spring.events.filtering.scanning;

import com.opencredo.concursus.domain.events.filtering.log.EventLogFilter;
import com.opencredo.concursus.domain.events.logging.EventLog;
import com.opencredo.concursus.spring.events.filtering.FilterOrdering;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ComponentScanningEventLogFilter implements ApplicationContextAware, EventLogFilter {

    private EventLogFilter composedFilter;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        composedFilter = EventLogFilter.compose(
                applicationContext.getBeansOfType(EventLogFilter.class).values().stream()
                .filter(filter -> filter != this)
                .sorted(FilterOrdering.filterOrderComparator)
                .toArray(EventLogFilter[]::new));
    }

    @Override
    public EventLog apply(EventLog eventLog) {
        return composedFilter.apply(eventLog);
    }
}
