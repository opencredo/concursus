package com.opencredo.concourse.domain.commands;

import com.opencredo.concourse.data.tuples.Tuple;
import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.common.VersionedName;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.domain.time.TimeUUID;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Command {

    public static Command of(AggregateId aggregateId, StreamTimestamp commandTimestamp, VersionedName commandName, Tuple parameters, Type resultType) {
        checkNotNull(aggregateId, "aggregateId must not be null");
        checkNotNull(commandTimestamp, "commandTimestamp must not be null");
        checkNotNull(commandName, "commandName must not be null");
        checkNotNull(parameters, "parameters must not be null");
        checkNotNull(resultType, "resultType must not be null");

        return new Command(aggregateId, commandTimestamp, Optional.empty(), commandName, parameters, resultType);
    }

    private final AggregateId aggregateId;
    private final StreamTimestamp commandTimestamp;
    private final Optional<UUID> processingId;
    private final VersionedName commandName;
    private final Tuple parameters;
    private final Type resultType;

    private Command(AggregateId aggregateId, StreamTimestamp commandTimestamp, Optional<UUID> processingId, VersionedName commandName, Tuple parameters, Type resultType) {
        this.aggregateId = aggregateId;
        this.commandTimestamp = commandTimestamp;
        this.processingId = processingId;
        this.commandName = commandName;
        this.parameters = parameters;
        this.resultType = resultType;
    }

    public Command processed(UUID processingId) {
        checkNotNull(processingId, "processingId must not be null");
        checkArgument(processingId.version() == 1, "processingId must be type 1 UUID");

        return new Command(aggregateId, commandTimestamp, Optional.of(processingId), commandName, parameters, resultType);
    }

    public Optional<Instant> getProcessingTime() {
        return processingId.map(TimeUUID::getInstant);
    }

    public AggregateId getAggregateId() {
        return aggregateId;
    }

    public StreamTimestamp getCommandTimestamp() {
        return commandTimestamp;
    }

    public Optional<UUID> getProcessingId() {
        return processingId;
    }

    public VersionedName getCommandName() {
        return commandName;
    }

    public Tuple getParameters() {
        return parameters;
    }

    public Type getResultType() {
        return resultType;
    }

    public CommandResult complete(Instant timeCompleted, Optional<Object> result) {
        return CommandResult.ofSuccess(
                processingId.orElseThrow(() ->
                        new IllegalStateException("Command result submitted but command has no processing id")),
                timeCompleted,
                resultType,
                result);
    }

    public CommandResult fail(Instant timeCompleted, Exception failure) {
        return CommandResult.ofFailure(
                processingId.orElseThrow(() ->
                        new IllegalStateException("Command result submitted but command has no processing id")),
                timeCompleted,
                resultType,
                failure);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Command && equals(Command.class.cast(o)));
    }

    private boolean equals(Command o) {
        return aggregateId.equals(o.aggregateId)
                && commandTimestamp.equals(o.commandTimestamp)
                && processingId.equals(o.processingId)
                && commandName.equals(o.commandName)
                && parameters.equals(o.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aggregateId, commandTimestamp, processingId, commandName, parameters);
    }

    @Override
    public String toString() {
        return getProcessingTime().map(processingTime ->
                String.format("%s %s\nat %s\nwith %s\nprocessed at %s",
                        aggregateId, commandName, commandTimestamp, parameters, processingTime))
                .orElseGet(() -> String.format("%s %s\nat %s\nwith %s",
                        aggregateId, commandName, commandTimestamp, parameters));
    }
}
