package com.opencredo.concursus.spring.commands.processing;

import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.dispatching.CommandProcessor;
import com.opencredo.concursus.mapping.commands.methods.dispatching.MethodDispatchingCommandProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
        methodDispatchingCommandProcessor.subscribe(CommandProcessorReflection.getHandlerInterface(handler), handler);
    }

    @Override
    public Optional<Object> process(Command command) throws Exception {
        return methodDispatchingCommandProcessor.process(command);
    }
}
