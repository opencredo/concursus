package com.opencredo.concourse.spring.filtering;

import com.opencredo.concourse.domain.events.logging.EventLog;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class EventLogFilteringBeanPostProcessor implements BeanPostProcessor {

    private final ComponentScanningEventLogFilter eventLogFilter;

    @Autowired
    public EventLogFilteringBeanPostProcessor(ComponentScanningEventLogFilter eventLogFilter) {
        this.eventLogFilter = eventLogFilter;
    }

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        return o;
    }

    @Override
    public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
        if (o instanceof EventLog) {
            return eventLogFilter.apply((EventLog) o);
        }
        return o;
    }
}
