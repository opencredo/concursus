package com.opencredo.concursus.hazelcast.commands;

import com.hazelcast.core.ManagedContext;
import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.dispatching.CommandProcessor;

/**
 * A {@link ManagedContext} that passes a {@link CommandProcessor} to any deserialised objects that implement
 * {@link CommandProcessorAware}.
 */
public final class CommandProcessingManagedContext implements ManagedContext {

    /**
     * Create a {@link ManagedContext} that passes the supplied {@link CommandProcessor} to any deserialised objects
     * that implement {@link CommandProcessorAware}.
     * @param commandProcessor The {@link CommandProcessor} to use to process {@link Command}s.
     * @return The constructed {@link ManagedContext}.
     */
    public static ManagedContext processingCommandsWith(CommandProcessor commandProcessor) {
        return new CommandProcessingManagedContext(commandProcessor);
    }

    private final CommandProcessor commandProcessor;

    private CommandProcessingManagedContext(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    @Override
    public Object initialize(Object o) {
        if (o instanceof CommandProcessorAware) {
            ((CommandProcessorAware) o).setCommandProcessor(commandProcessor);
        }
        return o;
    }
}
