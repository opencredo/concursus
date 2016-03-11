package com.opencredo.concourse.mapping.commands.methods.proxying;

import com.google.common.base.Preconditions;
import com.opencredo.concourse.domain.commands.channels.CommandOutChannel;

public final class CommandProxyFactory {

    public static CommandProxyFactory proxying(CommandOutChannel commandOutChannel) {
        return new CommandProxyFactory(commandOutChannel);
    }

    private CommandProxyFactory(CommandOutChannel commandOutChannel) {
        this.commandOutChannel = commandOutChannel;
    }

    private final CommandOutChannel commandOutChannel;

    public <T> T getProxy(Class<? extends T> commandInterface) {
        Preconditions.checkNotNull(commandInterface, "commandInterface must not be null");

        return CommandIssuingProxy.proxying(commandOutChannel, commandInterface);
    }

}
