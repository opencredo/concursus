package com.opencredo.concursus.domain.time;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The timestamp of an event within a stream of events.
 */
public final class StreamTimestamp implements Comparable<StreamTimestamp> {

    /**
     * Comparator which establishes a total ordering over {@link StreamTimestamp}s.
     */
    public static final Comparator<StreamTimestamp> COMPARATOR =
            Comparator.comparing(StreamTimestamp::getTimestamp).thenComparing(StreamTimestamp::getStreamId);

    /**
     * The current timestamp within the default (empty) stream.
     * @return The current timestamp within the default (empty) stream.
     */
    public static StreamTimestamp now() {
        return now("");
    }

    /**
     * The current timestamp within the specified stream.
     * @param streamId The stream to which the timestamp belongs.
     * @return The current timestamp within the specified stream.
     */
    public static StreamTimestamp now(String streamId) {
        return of(streamId, Instant.now());
    }

    /**
     * The timestamp at the given instant within the default (empty) stream.
     * @param timestamp The instant of the timestamp.
     * @return The timestamp at the given instant within the default (empty) stream.
     */
    public static StreamTimestamp of(Instant timestamp) {
        return of("", timestamp);
    }

    /**
     * The timestamp at the given instant within the given stream.
     * @param streamId The id of the stream to which the timestamp belongs.
     * @param timestamp The instant of the timestamp.
     * @return The timestamp at the given instant within the given stream.
     */
    public static StreamTimestamp of(String streamId, Instant timestamp) {
        checkNotNull(streamId, "streamId must not be null");
        checkNotNull(timestamp, "timestamp must not be null");

        return new StreamTimestamp(streamId, timestamp);
    }

    private final String streamId;

    private final Instant timestamp;

    private StreamTimestamp(String streamId, Instant timestamp) {
        this.streamId = streamId;
        this.timestamp = timestamp;
    }

    /**
     * Get the stream id.
     * @return The stream id.
     */
    public String getStreamId() {
        return streamId;
    }

    /**
     * Get the timestamp instant.
     * @return The timestamp instant.
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * This timestamp, but within a substream of this timestamp's stream.
     * @param substreamName The name of the substream.
     * @return This timestamp, but within the specified substream of this timestamp's stream.
     */
    public StreamTimestamp subStream(String substreamName) {
        return new StreamTimestamp(streamId + ":" + substreamName, timestamp);
    }

    /**
     * This timestamp, but in the future.
     * @param i The number of units into the future to move the timestamp instant.
     * @param unit The {@link ChronoUnit} to use.
     * @return The modified timestamp.
     */
    public StreamTimestamp plus(int i, ChronoUnit unit) {
        return new StreamTimestamp(streamId, timestamp.plus(i, unit));
    }

    /**
     * This timestamp, but in the past.
     * @param i The number of units into the past to move the timestamp instant.
     * @param unit The {@link ChronoUnit} to use.
     * @return The modified timestamp.
     */
    public StreamTimestamp minus(int i, ChronoUnit unit) {
        return new StreamTimestamp(streamId, timestamp.minus(i, unit));
    }

    @Override
    public int compareTo(StreamTimestamp o) {
        return COMPARATOR.compare(this, o);
    }

    @Override
    public boolean equals(Object o) {
        return this == o
                || (o instanceof StreamTimestamp
                    && ((StreamTimestamp) o).streamId.equals(streamId)
                    && ((StreamTimestamp) o).timestamp.equals(timestamp));
    }

    @Override
    public int hashCode() {
        return Objects.hash(streamId, timestamp);
    }

    @Override
    public String toString() {
        return timestamp + "/" + streamId;
    }

    public boolean isBefore(StreamTimestamp timestamp) {
        return compareTo(timestamp) < 0;
    }

    public boolean isAfter(StreamTimestamp timestamp) {
        return compareTo(timestamp) > 0;
    }
}
