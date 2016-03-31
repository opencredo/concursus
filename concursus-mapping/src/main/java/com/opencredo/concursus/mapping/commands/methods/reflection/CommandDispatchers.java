package com.opencredo.concursus.mapping.commands.methods.reflection;

import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandType;
import com.opencredo.concursus.domain.functional.CompletableFutures;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class CommandDispatchers {

    private CommandDispatchers() {
    }

    private static CommandDispatcher toMethod(Method method, CommandMethodMapping mapping) {
        return new MethodInvokingCommandDispatcher(method, mapping);
    }

    public static TypeMappingCommandDispatcher dispatchingCommandsByType(Map<Method, CommandMethodMapping> mappings) {
        return new TypeMappingCommandDispatcher(mappings.entrySet().stream().collect(Collectors.toMap(
                e -> e.getValue().getCommandType(),
                e -> toMethod(e.getKey(), e.getValue())
        )));
    }

    private static final class MethodInvokingCommandDispatcher implements CommandDispatcher {

        private final Method method;
        private final CommandMethodMapping commandMethodMapping;

        MethodInvokingCommandDispatcher(Method method, CommandMethodMapping commandMethodMapping) {
            this.method = method;
            this.commandMethodMapping = commandMethodMapping;
        }

        @Override
        public CompletableFuture<?> apply(Object target, Command command) {
            try {
                return CompletableFuture.class.cast(method.invoke(target, commandMethodMapping.mapCommand(command)));
            } catch (InvocationTargetException e) {
                return CompletableFutures.failing(e.getCause());
            } catch (IllegalAccessException e) {
                return CompletableFutures.failing(e);
            }
        }
    }

    private static final class TypeMappingCommandDispatcher implements MultiTypeCommandDispatcher {

        private final Map<CommandType, CommandDispatcher> dispatcherMap;

        private TypeMappingCommandDispatcher(Map<CommandType, CommandDispatcher> dispatcherMap) {
            this.dispatcherMap = dispatcherMap;
        }

        @Override
        public CompletableFuture<?> apply(Object o, Command command) {
            CommandDispatcher dispatcher = dispatcherMap.get(CommandType.of(command));

            return dispatcher.apply(o, command);
        }

        @Override
        public Set<CommandType> getHandledCommandTypes() {
            return dispatcherMap.keySet();
        }
    }
}
