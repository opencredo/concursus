package com.opencredo.concourse.mapping.commands.methods.proxying;

import com.google.common.base.Preconditions;
import com.opencredo.concourse.domain.commands.channels.CommandOutChannel;
import com.opencredo.concourse.mapping.commands.methods.reflection.CommandInterfaceInfo;
import com.opencredo.concourse.mapping.commands.methods.reflection.CommandMethodMapping;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * A proxy that converts method invocations into commands
 */
public final class CommandIssuingProxy implements InvocationHandler {

    /**
     * Create an CommandIssuingProxy proxying the given interface, and dispatching the issued commands via the supplied {@link CommandOutChannel}.
     * @param commandOutChannel The Consumer that will be called back with commands.
     * @param commandInterface The interface to proxy.
     * @param <T> The type of the interface to proxy.
     * @return The proxy instance.
     */
    public static <T> T proxying(CommandOutChannel commandOutChannel, Class<T> commandInterface) {
        return commandInterface.cast(Proxy.newProxyInstance(commandInterface.getClassLoader(),
                new Class<?>[] { commandInterface },
                new CommandIssuingProxy(commandOutChannel, CommandInterfaceInfo.forInterface(commandInterface).getCommandMappers())
        ));
    }

    private final CommandOutChannel commandOutChannel;
    private final Map<Method, CommandMethodMapping> commandMappers;

    private CommandIssuingProxy(CommandOutChannel commandOutChannel, Map<Method, CommandMethodMapping> commandMappers) {
        this.commandOutChannel = commandOutChannel;
        this.commandMappers = commandMappers;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().isAssignableFrom(getClass())) {
            return method.invoke(this, args);
        }

        CommandMethodMapping mapper = commandMappers.get(method);
        Preconditions.checkState(mapper != null, "No mapper found for method %s", method);

        return commandOutChannel.apply(mapper.mapArguments(args)).thenApply(result -> result.orElse(null));
    }
}
