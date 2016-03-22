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

/**
 * A command issued to the system.
 */
public final class Command {

    /**
     * Create a new command with the specified properties.
     * @param aggregateId The {@link AggregateId} of the aggregate to which the command is addressed.
     * @param commandTimestamp When the command was issued.
     * @param commandName The name of the command.
     * @param parameters The command's parameters, encoded as a {@link Tuple}.
     * @param resultType The {@link Type} returned when the command is processed.
     * @return The constructed {@link Command}.
     */
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

    /**
     * Return a copy of this {@link Command}, updated with a processing id which uniquely identifies the command and
     * indicates when it was processed.
     * @param processingId The processing id to add to the command.
     * @return The updated command.
     */
    public Command processed(UUID processingId) {
        checkNotNull(processingId, "processingId must not be null");
        checkArgument(processingId.version() == 1, "processingId must be type 1 UUID");

        return new Command(aggregateId, commandTimestamp, Optional.of(processingId), commandName, parameters, resultType);
    }

    /**
     * Get the processing time of the command.
     * @return If the command has been processed, return the processing time encoded in its processingId; otherwise
     * {@link Optional}::empty.
     */
    public Optional<Instant> getProcessingTime() {
        return processingId.map(TimeUUID::getInstant);
    }

    /**
     * Get the aggregate id of the command.
     * @return The aggregate id of the command.
     */
    public AggregateId getAggregateId() {
        return aggregateId;
    }

    /**
     * Get the command timestamp of the command.
     * @return The command timestamp of the command.
     */
    public StreamTimestamp getCommandTimestamp() {
        return commandTimestamp;
    }

    /**
     * Get the processing id of the command (if it has been processed).
     * @return The processing id of the command.
     */
    public Optional<UUID> getProcessingId() {
        return processingId;
    }

    /**
     * Get the name of the command.
     * @return The name of the command.
     */
    public VersionedName getCommandName() {
        return commandName;
    }

    /**
     * Get the command's parameters.
     * @return The command's parameters.
     */
    public Tuple getParameters() {
        return parameters;
    }

    /**
     * Get the command's result type.
     * @return The command's result type.
     */
    public Type getResultType() {
        return resultType;
    }

    /**
     * Create a {@link CommandResult} representing the successful completion of this command.
     * @param timeCompleted When processing of the command was completed.
     * @param result The result of the command's processing.
     * @return The constructed {@link CommandResult}.
     */
    public CommandResult complete(Instant timeCompleted, Optional<Object> result) {
        return CommandResult.ofSuccess(
                processingId.orElseThrow(() ->
                        new IllegalStateException("Command result submitted but command has no processing id")),
                timeCompleted,
                resultType,
                result);
    }

    /**
     * Create a {@link CommandResult} representing the unsuccessful completion of this command.
     * @param timeCompleted When processing of the command was interrupted.
     * @param failure The {@link Exception} thrown while processing the command.
     * @return The constructed {@link CommandResult}.
     */
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
