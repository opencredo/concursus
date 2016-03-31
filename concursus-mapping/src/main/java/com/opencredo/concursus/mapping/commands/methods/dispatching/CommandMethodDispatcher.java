package com.opencredo.concursus.mapping.commands.methods.dispatching;

import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.dispatching.CommandProcessor;
import com.opencredo.concursus.domain.commands.dispatching.CommandSubscribable;
import com.opencredo.concursus.mapping.commands.methods.reflection.CommandInterfaceInfo;
import com.opencredo.concursus.mapping.commands.methods.reflection.MultiTypeCommandDispatcher;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CommandMethodDispatcher implements CommandProcessor {

    public static <H> CommandMethodDispatcher toHandler(Class<? extends H> handlerInterface, H target) {
        checkNotNull(handlerInterface, "handlerInterface must not be null");
        checkNotNull(target, "target must not be null");

        return new CommandMethodDispatcher(target, CommandInterfaceInfo.forInterface(handlerInterface).getCommandDispatcher());
    }

    private final Object target;
    private final MultiTypeCommandDispatcher commandDispatcher;

    private CommandMethodDispatcher(Object target, MultiTypeCommandDispatcher commandDispatcher) {
        this.target = target;
        this.commandDispatcher = commandDispatcher;
    }

    @Override
    public Optional<Object> process(Command command) throws Exception {
        checkNotNull(command, "command must not be null");

        try {
            return Optional.ofNullable(commandDispatcher.apply(target, command).get());
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Exception) {
                throw Exception.class.cast(e.getCause());
            }
            throw e;
        }
    }

    public void subscribeTo(CommandSubscribable publisher) {
        commandDispatcher.getHandledCommandTypes().forEach(commandType -> publisher.subscribe(commandType, this));
    }
}
