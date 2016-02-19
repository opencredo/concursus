package com.opencredo.concourse.domain.commands;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class CommandResult {

    public static CommandResult ofSuccess(UUID processingId, Instant processedTimestamp, Type resultType, Optional<Object> resultValue) {
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
        return new CommandResult(processingId, processedTimestamp, resultType, resultValue, Optional.empty(), true);
    }

    public static CommandResult ofFailure(UUID processingId, Instant processedTimestamp, Type resultType, Exception failure) {
        checkNotNull(processingId, "processingId must not be null");
        checkNotNull(processedTimestamp, "processedTimestamp must not be null");
        checkNotNull(resultType, "resultType must not be null");
        checkNotNull(failure, "failure must not be null");

        return new CommandResult(processingId, processedTimestamp, resultType, Optional.empty(), Optional.of(failure), false);
    }

    private CommandResult(UUID processingId, Instant processedTimestamp, Type resultType, Optional<Object> resultValue, Optional<Exception> exception, boolean succeeded) {
        this.processingId = processingId;
        this.processedTimestamp = processedTimestamp;
        this.resultType = resultType;
        this.resultValue = resultValue;
        this.exception = exception;
        this.succeeded = succeeded;
    }

    private final UUID processingId;
    private final Instant processedTimestamp;
    private final Type resultType;
    private final Optional<Object> resultValue;
    private final Optional<Exception> exception;
    private final boolean succeeded;

    public UUID getProcessingId() {
        return processingId;
    }

    public Instant getProcessedTimestamp() {
        return processedTimestamp;
    }

    public Type getResultType() {
        return resultType;
    }

    public Optional<Object> getResultValue() {
        if (!succeeded) {
            throw new IllegalStateException("getResultValue called on failed command result");
        }
        return resultValue;
    }

    public Exception getException() {
        return exception.orElseThrow(() -> new IllegalStateException("getException called on successful command result"));
    }

    public Object get() throws Exception {
        if (succeeded) {
            return resultValue.orElse(null);
        }
        throw exception.orElseThrow(IllegalStateException::new);
    }

    public boolean succeeded() {
        return succeeded;
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
                && o.resultValue.equals(resultValue)
                && o.exception.equals(exception)
                && o.succeeded == succeeded;
    }

    @Override
    public int hashCode() {
        return Objects.hash(processingId, processedTimestamp, resultType, resultValue, exception, succeeded);
    }

    @Override
    public String toString() {
        return String.format("Command %s processed at %s with result %s",
                processingId, processedTimestamp, succeeded ? resultValue.orElse(null) : exception.get());
    }
}
