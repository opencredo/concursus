package com.opencredo.concursus.mapping.commands.methods.reflection;

import com.opencredo.concursus.domain.commands.CommandType;

import java.util.Set;

public interface MultiTypeCommandDispatcher extends CommandDispatcher {

    Set<CommandType> getHandledCommandTypes();

}
