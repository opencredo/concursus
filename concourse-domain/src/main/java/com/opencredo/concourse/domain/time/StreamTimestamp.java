package com.opencredo.concourse.domain.time;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public final class StreamTimestamp implements Comparable<StreamTimestamp> {

    public static final Comparator<StreamTimestamp> COMPARATOR =
            Comparator.comparing(StreamTimestamp::getTimestamp).thenComparing(StreamTimestamp::getStreamId);

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

    public String getStreamId() {
        return streamId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public StreamTimestamp subStream(String substreamName) {
        return new StreamTimestamp(streamId + ":" + substreamName, timestamp);
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
}
