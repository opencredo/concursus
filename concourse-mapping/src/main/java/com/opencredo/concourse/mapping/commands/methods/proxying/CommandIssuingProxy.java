package com.opencredo.concourse.mapping.commands.methods.proxying;

import com.google.common.base.Preconditions;
import com.opencredo.concourse.domain.commands.dispatching.CommandBus;
import com.opencredo.concourse.mapping.commands.methods.reflection.CommandInterfaceReflection;
import com.opencredo.concourse.mapping.commands.methods.reflection.CommandMethodMapping;
import com.opencredo.concourse.mapping.commands.methods.reflection.Nonchalantly;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * A proxy that converts method invocations into commands
 */
public final class CommandIssuingProxy implements InvocationHandler {

    /**
     * Create an CommandIssuingProxy proxying the given class, and dispatching the issued commands via the supplied command bus.
     * @param commandBus The Consumer that will be called back with commands.
     * @param klass The class to proxy.
     * @param <T> The type of the class to proxy.
     * @return The proxy instance.
     */
    public static <T> T proxying(CommandBus commandBus, Class<T> klass) {
        return klass.cast(Proxy.newProxyInstance(klass.getClassLoader(),
                new Class<?>[] { klass },
                new CommandIssuingProxy(commandBus, CommandInterfaceReflection.getCommandMappers(klass))
        ));
    }

    private final CommandBus commandBus;
    private final Map<Method, CommandMethodMapping> commandMappers;

    private CommandIssuingProxy(CommandBus commandBus, Map<Method, CommandMethodMapping> commandMappers) {
        this.commandBus = commandBus;
        this.commandMappers = commandMappers;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().isAssignableFrom(getClass())) {
            return method.invoke(this, args);
        }

        CommandMethodMapping mapper = commandMappers.get(method);
        Preconditions.checkState(mapper != null, "No mapper found for method %s", method);

        try {
            return commandBus.apply(mapper.mapArguments(args)).thenApply(result -> (Object) Nonchalantly.invoke(result::get));
        } catch (Exception e) {
            CompletableFuture<?> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}
