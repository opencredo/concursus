package com.opencredo.concourse.mapping.commands.methods.reflection;

import com.opencredo.concourse.domain.commands.Command;
import com.opencredo.concourse.domain.commands.CommandType;
import com.opencredo.concourse.domain.common.VersionedName;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesCommandsFor;
import com.opencredo.concourse.mapping.annotations.Name;
import com.opencredo.concourse.mapping.reflection.MethodInvoking;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CommandInterfaceReflection {

    public static String getAggregateType(Class<?> klass) {
        return klass.getAnnotation(HandlesCommandsFor.class).value();
    }

    public static VersionedName getCommandName(Method method) {
        return method.isAnnotationPresent(Name.class)
                ? VersionedName.of(
                method.getAnnotation(Name.class).value(),
                method.getAnnotation(Name.class).version())
                : VersionedName.of(method.getName(), "0");
    }

    private static final ConcurrentMap<Class<?>, Map<Method, CommandMethodMapping>> cache = new ConcurrentHashMap<>();

    public static Map<Method, CommandMethodMapping> getCommandMappers(Class<?> klass) {
        return cache.computeIfAbsent(klass, CommandInterfaceReflection::getCommandMappersUncached);
    }

    private static Map<Method, CommandMethodMapping> getCommandMappersUncached(Class<?> klass) {
        return Stream.of(klass.getMethods())
                .filter(CommandInterfaceReflection::isCommandEmittingMethod)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        CommandMethodMapping::forMethod
                ));
    }

    private static boolean isCommandEmittingMethod(Method method) {
        return method.getReturnType().equals(CompletableFuture.class)
                && method.getParameterCount() >= 2
                && method.getParameterTypes()[0].equals(StreamTimestamp.class)
                && method.getParameterTypes()[1].equals(UUID.class);
    }

    private static final ConcurrentMap<Class<?>, Map<CommandType, BiFunction<Object, Command, CompletableFuture<?>>>> commandDispatcherCache = new ConcurrentHashMap<>();

    public static Map<CommandType, BiFunction<Object, Command, CompletableFuture<?>>> getCommandDispatchers(Class<?> klass) {
        return commandDispatcherCache.computeIfAbsent(klass, CommandInterfaceReflection::getCommandDispatchersUncached);
    }

    public static Map<CommandType, BiFunction<Object, Command, CompletableFuture<?>>> getCommandDispatchersUncached(Class<?> klass) {
        Map<Method, CommandMethodMapping> methodMappings = getCommandMappers(klass);

        return methodMappings.entrySet().stream().collect(Collectors.toMap(
                e -> e.getValue().getCommandType(),
                e -> getCommandDispatcher(e.getKey(), e.getValue())
        ));
    }

    private static BiFunction<Object, Command, CompletableFuture<?>> getCommandDispatcher(Method method, CommandMethodMapping commandMethodMapping) {
        BiFunction<Object, Object[], CompletableFuture> invoker = MethodInvoking.invokingInstance(CompletableFuture.class, method);
        return (target, command) -> {
            try {
                return invoker.apply(target, commandMethodMapping.mapCommand(command));
            } catch (RuntimeException e) {
                return CompletableFutures.failing(e.getCause());
            }
        };
    }

}
