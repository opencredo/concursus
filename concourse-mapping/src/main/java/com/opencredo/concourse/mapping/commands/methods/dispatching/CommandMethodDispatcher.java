package com.opencredo.concourse.mapping.commands.methods.dispatching;

import com.google.common.base.Preconditions;
import com.opencredo.concourse.domain.commands.Command;
import com.opencredo.concourse.domain.commands.CommandType;
import com.opencredo.concourse.domain.commands.dispatching.CommandProcessor;
import com.opencredo.concourse.domain.commands.dispatching.CommandSubscribable;
import com.opencredo.concourse.mapping.commands.methods.reflection.CommandInterfaceReflection;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CommandMethodDispatcher implements CommandProcessor {

    public static <H> CommandMethodDispatcher toHandler(Class<? extends H> handlerInterface, H target) {
        checkNotNull(handlerInterface, "handlerInterface must not be null");
        checkNotNull(target, "target must not be null");

        return new CommandMethodDispatcher(target, CommandInterfaceReflection.getCommandDispatchers(handlerInterface));
    }

    private final Object target;
    private final Map<CommandType, BiFunction<Object, Command, CompletableFuture<?>>> commandMappers;

    private CommandMethodDispatcher(Object target, Map<CommandType, BiFunction<Object, Command, CompletableFuture<?>>> commandMappers) {
        this.target = target;
        this.commandMappers = commandMappers;
    }

    @Override
    public Optional<Object> process(Command command) throws Exception {
        checkNotNull(command, "command must not be null");

        BiFunction<Object, Command, CompletableFuture<?>> methodDispatcher = commandMappers.get(CommandType.of(command));
        Preconditions.checkState(methodDispatcher != null,
                "No method dispatcher found for command %s", command);

        try {
            return Optional.ofNullable(methodDispatcher.apply(target, command).get());
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Exception) {
                throw Exception.class.cast(e.getCause());
            }
            throw e;
        }
    }

    public void subscribeTo(CommandSubscribable publisher) {
        commandMappers.keySet().forEach(commandType -> publisher.subscribe(commandType, this));
    }
}
