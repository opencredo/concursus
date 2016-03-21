package com.opencredo.concourse.domain.commands;

import com.opencredo.concourse.data.tuples.Tuple;
import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.common.VersionedName;
import com.opencredo.concourse.domain.time.StreamTimestamp;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CommandType {

    private static Type resultType;

    public static CommandType of(Command command) {
        checkNotNull(command, "command must not be null");

        return of(command.getAggregateId().getType(), command.getCommandName());
    }

    public static CommandType of(String aggregateType, VersionedName commandName) {
        checkNotNull(aggregateType, "aggregateType must not be null");
        checkNotNull(commandName, "commandName must not be null");

        return new CommandType(aggregateType, commandName);
    }

    private final String aggregateType;
    private final VersionedName commandName;

    private CommandType(String aggregateType, VersionedName commandName) {
        this.aggregateType = aggregateType;
        this.commandName = commandName;
    }


    @Override
    public boolean equals(Object o) {
        return this == o ||
                (o instanceof CommandType
                        && ((CommandType) o).aggregateType.equals(aggregateType)
                        && ((CommandType) o).commandName.equals(commandName));
    }

    @Override
    public int hashCode() {
        return Objects.hash(aggregateType, commandName);
    }

    @Override
    public String toString() {
        return aggregateType + "/" + commandName.getFormatted();
    }


    public Command makeCommand(UUID uuid, StreamTimestamp timestamp, Tuple parameters, Type resultType) {
        return Command.of(
                AggregateId.of(aggregateType, uuid),
                timestamp,
                commandName,
                parameters,
                resultType);
    }
}
