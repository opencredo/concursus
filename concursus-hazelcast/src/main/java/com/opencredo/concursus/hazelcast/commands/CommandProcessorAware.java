package com.opencredo.concursus.hazelcast.commands;

import com.opencredo.concursus.domain.commands.dispatching.CommandProcessor;

/**
 * Interface of an object that can be configured with a {@link CommandProcessor} after deserialisation to a Hazelcast
 * node.
 */
@FunctionalInterface
public interface CommandProcessorAware {
    void setCommandProcessor(CommandProcessor commandProcessor);
}
