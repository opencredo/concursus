package com.opencredo.concourse.domain.commands.dispatching;

import com.opencredo.concourse.domain.commands.Command;

import java.util.Optional;

public interface CommandProcessor {

    Optional<Object> process(Command command) throws Exception;

}
