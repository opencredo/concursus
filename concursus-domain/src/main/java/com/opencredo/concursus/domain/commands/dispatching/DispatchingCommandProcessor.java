package com.opencredo.concursus.domain.commands.dispatching;

import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandType;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DispatchingCommandProcessor implements CommandProcessor, CommandSubscribable {

    public static DispatchingCommandProcessor create() {
        return new DispatchingCommandProcessor(new ConcurrentHashMap<>());
    }

    private final ConcurrentMap<CommandType, CommandProcessor> processorRegistry;

    private DispatchingCommandProcessor(ConcurrentMap<CommandType, CommandProcessor> processorRegistry) {
        this.processorRegistry = processorRegistry;
    }

    @Override
    public Optional<Object> process(Command command) throws Exception {
        final CommandType commandType = CommandType.of(command);
        CommandProcessor processor = processorRegistry.get(commandType);
        checkNotNull(processor,
                "No command processor registered for command type %s", commandType);

        return processor.process(command);
    }

    @Override
    public CommandSubscribable subscribe(CommandType commandType, CommandProcessor commandProcessor) {
        processorRegistry.put(commandType, commandProcessor);
        return this;
    }
}
