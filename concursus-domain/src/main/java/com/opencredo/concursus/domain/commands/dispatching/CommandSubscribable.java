package com.opencredo.concursus.domain.commands.dispatching;

import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandType;

/**
 * An object that can subscribe {@link CommandProcessor}s to handle {@link Command}s by {@link CommandType}
 */
@FunctionalInterface
public interface CommandSubscribable {

    /**
     * Subscribe the given {@link CommandProcessor} to handle commands of the given {@link CommandType}
     * @param commandType The {@link CommandType} to handle.
     * @param commandProcessor The {@link CommandProcessor} to process commands of this type with.
     * @return This object, for method chaining.
     */
    CommandSubscribable subscribe(CommandType commandType, CommandProcessor commandProcessor);

}
