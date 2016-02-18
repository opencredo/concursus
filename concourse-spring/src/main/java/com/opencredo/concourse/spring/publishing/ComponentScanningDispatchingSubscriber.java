package com.opencredo.concourse.spring.publishing;

import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.methods.DispatchingSubscriber;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class ComponentScanningDispatchingSubscriber implements ApplicationContextAware {

    private final DispatchingSubscriber dispatchingSubscriber;

    @Autowired
    public ComponentScanningDispatchingSubscriber(DispatchingSubscriber dispatchingSubscriber) {
        this.dispatchingSubscriber = dispatchingSubscriber;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        applicationContext.getBeansWithAnnotation(EventHandler.class).values().forEach(this::subscribeHandler);
    }

    private void subscribeHandler(Object handler) {
        Class<?> eventInterface = Stream.of(handler.getClass().getInterfaces())
                .filter(iface -> iface.isAnnotationPresent(HandlesEventsFor.class))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No event handling interface found for " + handler.getClass()));

        dispatchingSubscriber.subscribe(eventInterface, handler);


    }
}
