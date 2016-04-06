package com.opencredo.concursus.mapping.commands.methods.dispatching;

@FunctionalInterface
public interface CommandHandlerSubscribable {

    /**
     * Subscribe a command handler to this object.
     * @param handlerClass The command-issuing interface to dispatch commands for.
     * @param commandHandler The command handler that implements the interface.
     * @param <H> The type of the command-issuing interface.
     * @return This object, for method chaining.
     */
    <H> CommandHandlerSubscribable subscribe(Class<? extends H> handlerClass, H commandHandler);

}
