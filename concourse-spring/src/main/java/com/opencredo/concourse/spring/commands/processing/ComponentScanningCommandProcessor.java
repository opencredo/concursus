package com.opencredo.concourse.spring.commands.processing;

import com.opencredo.concourse.domain.commands.Command;
import com.opencredo.concourse.domain.commands.dispatching.CommandProcessor;
import com.opencredo.concourse.mapping.annotations.HandlesCommandsFor;
import com.opencredo.concourse.mapping.commands.methods.dispatching.MethodDispatchingCommandProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

@Component
public class ComponentScanningCommandProcessor implements CommandProcessor, ApplicationContextAware {

    private final MethodDispatchingCommandProcessor methodDispatchingCommandProcessor;

    @Autowired
    public ComponentScanningCommandProcessor(MethodDispatchingCommandProcessor methodDispatchingCommandProcessor) {
        this.methodDispatchingCommandProcessor = methodDispatchingCommandProcessor;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        applicationContext.getBeansWithAnnotation(CommandHandler.class).values().forEach(this::subscribeHandler);
    }

    private void subscribeHandler(Object handler) {
        Class<?> eventInterface = Stream.of(handler.getClass().getInterfaces())
                .filter(iface -> iface.isAnnotationPresent(HandlesCommandsFor.class))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No command handling interface found for " + handler.getClass()));

        methodDispatchingCommandProcessor.subscribe(eventInterface, handler);
    }

    @Override
    public Optional<Object> process(Command command) throws Exception {
        return methodDispatchingCommandProcessor.process(command);
    }
}
