package com.opencredo.concourse.mapping.commands.methods.dispatching;

import com.opencredo.concourse.domain.commands.Command;
import com.opencredo.concourse.domain.commands.dispatching.CommandProcessor;
import com.opencredo.concourse.domain.commands.dispatching.DispatchingCommandProcessor;

import java.util.Optional;

public class MethodDispatchingCommandProcessor implements CommandProcessor {

    public static MethodDispatchingCommandProcessor dispatchingTo(DispatchingCommandProcessor dispatchingCommandProcessor) {
        return new MethodDispatchingCommandProcessor(dispatchingCommandProcessor);
    }

    private final DispatchingCommandProcessor dispatchingCommandProcessor;

    private MethodDispatchingCommandProcessor(DispatchingCommandProcessor dispatchingCommandProcessor) {
        this.dispatchingCommandProcessor = dispatchingCommandProcessor;
    }

    public <H> MethodDispatchingCommandProcessor subscribe(Class<? extends H> handlerClass, H commandProcessor) {
        CommandMethodDispatcher.toHandler(handlerClass, commandProcessor).subscribeTo(dispatchingCommandProcessor);
        return this;
    }

    @Override
    public Optional<Object> process(Command command) throws Exception {
        return dispatchingCommandProcessor.process(command);
    }
}
