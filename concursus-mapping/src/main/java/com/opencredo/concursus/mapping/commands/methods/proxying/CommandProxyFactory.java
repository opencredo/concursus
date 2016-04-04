package com.opencredo.concursus.mapping.commands.methods.proxying;

import com.google.common.base.Preconditions;
import com.opencredo.concursus.domain.commands.channels.CommandOutChannel;

/**
 * A factory for proxy objects that dispatch commands to the supplied {@link CommandOutChannel}.
 */
public final class CommandProxyFactory {

    /**
     * Create a factory for proxy objects that dispatch commands to the supplied {@link CommandOutChannel}.
     * @param commandOutChannel The {@link CommandOutChannel} to send commands to.
     * @return The constructed {@link CommandProxyFactory}.
     */
    public static CommandProxyFactory proxying(CommandOutChannel commandOutChannel) {
        Preconditions.checkNotNull(commandOutChannel, "commandOutChannel must not be null");

        return new CommandProxyFactory(commandOutChannel);
    }

    private CommandProxyFactory(CommandOutChannel commandOutChannel) {
        this.commandOutChannel = commandOutChannel;
    }

    private final CommandOutChannel commandOutChannel;

    /**
     * Create a proxy for the supplied command-issuing interface that sends commands to this object's wrapped
     * {@link CommandOutChannel}.
     * @param commandInterface The interface to create a proxy for.
     * @param <T> The type of the proxy.
     * @return The constructed proxy.
     */
    public <T> T getProxy(Class<? extends T> commandInterface) {
        Preconditions.checkNotNull(commandInterface, "commandInterface must not be null");

        return CommandIssuingProxy.proxying(commandOutChannel, commandInterface);
    }

}
