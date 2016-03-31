package com.opencredo.concursus.domain.commands;

import java.util.Map;
import java.util.Optional;

@FunctionalInterface
public interface CommandTypeMatcher {

    static CommandTypeMatcher matchingAgainst(Map<CommandType, CommandTypeInfo> map) {
        return commandType -> Optional.ofNullable(map.get(commandType));
    }

    Optional<CommandTypeInfo> match(CommandType commandType);

}
