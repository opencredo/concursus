package com.opencredo.concourse.domain.commands;

import com.google.common.reflect.TypeToken;
import com.opencredo.concourse.domain.functional.Either;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The result of processing a {@link Command}, which may be either success or failure.
 */
public final class CommandResult {

    static CommandResult ofSuccess(UUID processingId, Instant processedTimestamp, Type resultType, Optional<Object> resultValue) {
        checkNotNull(processingId, "processingId must not be null");
        checkNotNull(processedTimestamp, "processedTimestamp must not be null");
        checkNotNull(resultType, "resultType must not be null");
        checkNotNull(resultValue, "resultValue must not be null");

        if (resultType.equals(Void.class)) {
            checkArgument(!resultValue.isPresent(),
                    "resultValue must be empty if result type is void");
        } else {
            checkArgument(resultValue.isPresent(),
                    "resultValue must be present if result type is not void");
            resultValue.ifPresent(v -> checkArgument(TypeToken.of(resultType).getRawType().isAssignableFrom(v.getClass()),
                    "%s cannot be assigned to a command result of type %s", v, resultType));
        }
        return new CommandResult(processingId, processedTimestamp, resultType, Either.ofLeft(resultValue));
    }

    static CommandResult ofFailure(UUID processingId, Instant processedTimestamp, Type resultType, Exception failure) {
        checkNotNull(processingId, "processingId must not be null");
        checkNotNull(processedTimestamp, "processedTimestamp must not be null");
        checkNotNull(resultType, "resultType must not be null");
        checkNotNull(failure, "failure must not be null");

        return new CommandResult(processingId, processedTimestamp, resultType, Either.ofRight(failure));
    }

    private CommandResult(UUID processingId, Instant processedTimestamp, Type resultType, Either<Optional<Object>, Exception> result) {
        this.processingId = processingId;
        this.processedTimestamp = processedTimestamp;
        this.resultType = resultType;
        this.result = result;
    }

    private final UUID processingId;
    private final Instant processedTimestamp;
    private final Type resultType;
    private final Either<Optional<Object>, Exception> result;

    public UUID getProcessingId() {
        return processingId;
    }

    public Instant getProcessedTimestamp() {
        return processedTimestamp;
    }

    public Type getResultType() {
        return resultType;
    }

    public <T> T join(Function<Optional<Object>, T> left, Function<Exception, T> right) {
        return result.join(left, right);
    }

    public boolean succeeded() {
        return result.isLeft();
    }

    @Override
    public boolean equals(Object o) {
        return o == this ||
                (o instanceof CommandResult && equals((CommandResult) o));
    }

    private boolean equals(CommandResult o) {
        return o.processingId.equals(processingId)
                && o.processedTimestamp.equals(processedTimestamp)
                && o.resultType.equals(resultType)
                && o.result.equals(result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processingId, processedTimestamp, resultType, result);
    }

    @Override
    public String toString() {
        return String.format("Command %s processed at %s with result %s",
                processingId, processedTimestamp, result.join(Object::toString, Object::toString));
    }
}
