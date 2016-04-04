package com.opencredo.concursus.domain.commands.dispatching;

import com.opencredo.concursus.domain.commands.Command;

import java.util.Optional;

/**
 * A function that processes a {@link Command}, and may throw an {@link Exception}
 */
@FunctionalInterface
public interface CommandProcessor {

    /**
     * Process the supplied {@link Command}
     * @param command The {@link Command} to process
     * @return The result, if present.
     * @throws Exception Any exception thrown while processing the command.
     */
    Optional<Object> process(Command command) throws Exception;

}
