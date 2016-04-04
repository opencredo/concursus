package com.opencredo.concursus.domain.commands;

import java.util.Map;
import java.util.Optional;


/**
 * Provides {@link CommandTypeInfo}, for each {@link CommandType} that it knows about.
 */
@FunctionalInterface
public interface CommandTypeMatcher {

    /**
     * Create an {@link CommandType} that looks up {@link CommandTypeInfo} in the supplied {@link Map}.
     * @param map The {@link Map} to look up {@link CommandTypeInfo}s in.
     * @return The constructed {@link CommandTypeMatcher}.
     */
    static CommandTypeMatcher matchingAgainst(Map<CommandType, CommandTypeInfo> map) {
        return commandType -> Optional.ofNullable(map.get(commandType));
    }

    /**
     * Return the {@link CommandTypeInfo} matching the supplied {@link CommandType}, if known, or {@link Optional}::empty otherwise.
     * @param commandType The {@link CommandType} to find a {@link CommandTypeInfo} for.
     * @return The matching {@link CommandTypeInfo}, if present.
     */
    Optional<CommandTypeInfo> match(CommandType commandType);

}
