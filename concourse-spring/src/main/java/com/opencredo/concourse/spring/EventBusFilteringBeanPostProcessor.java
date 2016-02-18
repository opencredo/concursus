package com.opencredo.concourse.spring;

import com.opencredo.concourse.domain.events.dispatching.EventBus;
import com.opencredo.concourse.mapping.methods.ProxyingEventBus;
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
