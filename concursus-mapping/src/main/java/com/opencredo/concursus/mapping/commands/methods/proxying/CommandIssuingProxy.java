package com.opencredo.concursus.mapping.commands.methods.proxying;

import com.google.common.base.Preconditions;
import com.opencredo.concursus.domain.commands.channels.CommandOutChannel;
import com.opencredo.concursus.mapping.commands.methods.reflection.CommandInterfaceInfo;
import com.opencredo.concursus.mapping.commands.methods.reflection.CommandMethodMapping;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

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
        return proxying(commandOutChannel, commandInterface, 30000);
    }

    /**
     * Create an CommandIssuingProxy proxying the given interface, and dispatching the issued commands via the supplied {@link CommandOutChannel}.
     * @param commandOutChannel The Consumer that will be called back with commands.
     * @param commandInterface The interface to proxy.
     * @param timeoutMs The number of milliseconds to wait before timing out a command.
     * @param <T> The type of the interface to proxy.
     * @return The proxy instance.
     */
    public static <T> T proxying(CommandOutChannel commandOutChannel, Class<T> commandInterface, long timeoutMs) {
        return commandInterface.cast(Proxy.newProxyInstance(commandInterface.getClassLoader(),
                new Class<?>[] { commandInterface },
                new CommandIssuingProxy(commandOutChannel, CommandInterfaceInfo.forInterface(commandInterface).getCommandMappers(), timeoutMs)
        ));
    }

    private final CommandOutChannel commandOutChannel;
    private final Map<Method, CommandMethodMapping> commandMappers;
    private final long timeoutMs;

    private CommandIssuingProxy(CommandOutChannel commandOutChannel, Map<Method, CommandMethodMapping> commandMappers, long timeoutMs) {
        this.commandOutChannel = commandOutChannel;
        this.commandMappers = commandMappers;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().isAssignableFrom(getClass())) {
            try {
                return method.invoke(this, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        CommandMethodMapping mapper = commandMappers.get(method);
        Preconditions.checkState(mapper != null, "No mapper found for method %s", method);

        try {
            return commandOutChannel.apply(mapper.mapArguments(args))
                    .thenApply(result -> result.orElse(null))
                    .get(timeoutMs, MILLISECONDS);
        } catch (ExecutionException e) {
            throw new CommandExecutionException(e.getCause());
        } catch (InterruptedException | TimeoutException e) {
            throw new CommandExecutionException(e);
        }
    }
}
