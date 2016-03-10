package com.opencredo.concourse.mapping.commands.methods.reflection;

import com.opencredo.concourse.domain.commands.CommandType;
import com.opencredo.concourse.domain.commands.CommandTypeInfo;
import com.opencredo.concourse.mapping.annotations.HandlesCommandsFor;
import com.opencredo.concourse.mapping.reflection.MethodSelectors;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class CommandInterfaceInfo {

    private static final ConcurrentMap<Class<?>, CommandInterfaceInfo> cache = new ConcurrentHashMap<>();

    public static CommandInterfaceInfo forInterface(Class<?> iface) {
        return cache.computeIfAbsent(iface, CommandInterfaceInfo::forInterfaceUncached);
    }

    private static CommandInterfaceInfo forInterfaceUncached(Class<?> iface) {
        checkNotNull(iface, "iface must not be null");
        checkArgument(iface.isInterface(), "iface must be interface");
        checkArgument(iface.isAnnotationPresent(HandlesCommandsFor.class),
                "Interface %s is not annotated with @HandlesCommandsFor", iface);

        String aggregateType = iface.getAnnotation(HandlesCommandsFor.class).value();
        Map<Method, CommandMethodMapping> commandMappers = getCommandMappers(iface, aggregateType);
        Map<CommandType, CommandTypeInfo> typeInfoMap = commandMappers.values().stream().collect(Collectors.toMap(
                CommandMethodMapping::getCommandType,
                CommandMethodMapping::getCommandTypeInfo
        ));
        CommandDispatcher commandDispatcher = CommandDispatchers.dispatchingCommandsByType(commandMappers);


    }

    private static Map<Method, CommandMethodMapping> getCommandMappers(Class<?> iface, String aggregateType) {
            return Stream.of(iface.getMethods())
                    .filter(MethodSelectors.isCommandIssuingMethod)
                    .distinct()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            method -> CommandMethodMapping.forMethod(method, aggregateType)
                    ));
        }
    }
}
