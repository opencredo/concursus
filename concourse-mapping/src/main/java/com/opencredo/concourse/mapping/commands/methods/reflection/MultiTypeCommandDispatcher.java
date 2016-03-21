package com.opencredo.concourse.mapping.commands.methods.reflection;

import com.opencredo.concourse.domain.commands.CommandType;

import java.util.Set;

public interface MultiTypeCommandDispatcher extends CommandDispatcher {

    Set<CommandType> getHandledCommandTypes();

}
