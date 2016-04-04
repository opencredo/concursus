package com.opencredo.concursus.spring.commands;

import com.opencredo.concursus.domain.commands.dispatching.*;
import com.opencredo.concursus.domain.commands.filters.LoggingCommandExecutorFilter;
import com.opencredo.concursus.mapping.commands.methods.dispatching.MethodDispatchingCommandProcessor;
import com.opencredo.concursus.mapping.commands.methods.proxying.CommandProxyFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandSystemBeans {

    private final DispatchingCommandProcessor dispatchingCommandProcessor = DispatchingCommandProcessor.create();

    @Bean
    public CommandLog commandLog() {
        return new Slf4jCommandLog();
    }

    @Bean
    public CommandProcessor commandProcessor() {
        return dispatchingCommandProcessor;
    }

    @Bean
    public MethodDispatchingCommandProcessor methodDispatchingCommandProcessor() {
        return MethodDispatchingCommandProcessor.dispatchingTo(dispatchingCommandProcessor);
    }

    @Bean
    public CommandExecutor commandExecutor(CommandProcessor commandProcessor) {
        return ProcessingCommandExecutor.processingWith(commandProcessor);
    }

    @Bean
    public CommandBus commandBus(CommandLog commandLog, CommandExecutor commandExecutor) {
        return CommandBus.executingWith(
                LoggingCommandExecutorFilter.using(commandLog).apply(commandExecutor));
    }

    @Bean
    public CommandProxyFactory proxyingCommandOutChannel(CommandBus commandBus) {
        return CommandProxyFactory.proxying(commandBus.toCommandOutChannel());
    }

}
