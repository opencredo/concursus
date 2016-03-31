package com.opencredo.concursus.mapping.commands.methods.dispatching;

import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.dispatching.CommandProcessor;
import com.opencredo.concursus.domain.commands.dispatching.DispatchingCommandProcessor;

import java.util.Optional;

/**
 * A {@link CommandProcessor} that dispatches commands to methods on subscribed command handlers.
 */
public final class MethodDispatchingCommandProcessor implements CommandProcessor {

    /**
     * Create a {@link MethodDispatchingCommandProcessor} that wraps a {@link DispatchingCommandProcessor} to dispatch
     * commands to methods on subscribed command handlers.
     * @param dispatchingCommandProcessor The {@link DispatchingCommandProcessor} to wrap.
     * @return The constructed {@link MethodDispatchingCommandProcessor}.
     */
    public static MethodDispatchingCommandProcessor dispatchingTo(DispatchingCommandProcessor dispatchingCommandProcessor) {
        return new MethodDispatchingCommandProcessor(dispatchingCommandProcessor);
    }

    private final DispatchingCommandProcessor dispatchingCommandProcessor;

    private MethodDispatchingCommandProcessor(DispatchingCommandProcessor dispatchingCommandProcessor) {
        this.dispatchingCommandProcessor = dispatchingCommandProcessor;
    }

    /**
     * Subscribe a command handler to the wrapped {@link DispatchingCommandProcessor}.
     * @param handlerClass The command-issuing interface to dispatch commands for.
     * @param commandHandler The command handler that implements the interface.
     * @param <H> The type of the command-issuing interface.
     * @return This object, for method chaining.
     */
    public <H> MethodDispatchingCommandProcessor subscribe(Class<? extends H> handlerClass, H commandHandler) {
        CommandMethodDispatcher.toHandler(handlerClass, commandHandler).subscribeTo(dispatchingCommandProcessor);
        return this;
    }

    @Override
    public Optional<Object> process(Command command) throws Exception {
        return dispatchingCommandProcessor.process(command);
    }
}
