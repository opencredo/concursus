package com.opencredo.concursus.domain.commands.dispatching;

import com.opencredo.concursus.domain.commands.Command;

import java.util.Optional;

public interface CommandProcessor {

    Optional<Object> process(Command command) throws Exception;

}
