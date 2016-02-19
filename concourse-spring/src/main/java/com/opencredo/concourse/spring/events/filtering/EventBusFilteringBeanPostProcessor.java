package com.opencredo.concourse.spring.events.filtering;

import com.opencredo.concourse.domain.events.dispatching.EventBus;
import com.opencredo.concourse.mapping.events.methods.proxying.ProxyingEventBus;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class EventBusFilteringBeanPostProcessor implements BeanPostProcessor {

    private final ComponentScanningEventBusFilter eventBusFilter;

    @Autowired
    public EventBusFilteringBeanPostProcessor(ComponentScanningEventBusFilter eventBusFilter) {
        this.eventBusFilter = eventBusFilter;
    }

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        return o;
    }

    @Override
    public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
        if (o instanceof ProxyingEventBus) {
            return ProxyingEventBus.proxying(eventBusFilter.apply(((EventBus) o)));
        }
        if (o instanceof EventBus) {
            return eventBusFilter.apply((EventBus) o);
        }
        return o;
    }
}
