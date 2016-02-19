package com.opencredo.concourse.mapping.commands.methods.proxying;

import com.opencredo.concourse.domain.commands.dispatching.CommandBus;

public interface ProxyingCommandBus extends CommandBus {

    static ProxyingCommandBus proxying(CommandBus commandBus) {
        return commandBus::apply;
    }

    default <T> T getDispatcherFor(Class<? extends T> klass) {
        return CommandIssuingProxy.proxying(this, klass);
    }
}
