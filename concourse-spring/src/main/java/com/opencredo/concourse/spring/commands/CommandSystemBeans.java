package com.opencredo.concourse.spring.commands;

import com.opencredo.concourse.domain.commands.channels.CommandOutChannel;
import com.opencredo.concourse.domain.commands.dispatching.*;
import com.opencredo.concourse.mapping.commands.methods.dispatching.MethodDispatchingCommandProcessor;
import com.opencredo.concourse.mapping.commands.methods.proxying.CommandProxyFactory;
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
        return SynchronousCommandExecutor.processingWith(commandProcessor);
    }

    @Bean
    public CommandBus commandBus(CommandLog commandLog, CommandExecutor commandExecutor) {
        return LoggingCommandBus.using(commandLog, commandExecutor);
    }

    @Bean
    public CommandProxyFactory proxyingCommandOutChannel(CommandBus commandBus) {
        return CommandProxyFactory.proxying(CommandOutChannel.toBus(commandBus));
    }

}
