package com.opencredo.concourse.domain.time;

import java.time.Instant;

public final class TimeRangeBound {

    public static TimeRangeBound inclusive(Instant instant) {
        return new TimeRangeBound(instant, true);
    }

    public static TimeRangeBound exclusive(Instant instant) {
        return new TimeRangeBound(instant, false);
    }

    private final Instant instant;
    private final boolean isInclusive;

    private TimeRangeBound(Instant instant, boolean isInclusive) {
        this.instant = instant;
        this.isInclusive = isInclusive;
    }

    public Instant getInstant() {
        return instant;
    }

    public boolean isInclusive() {
        return isInclusive;
    }

    public boolean containsLower(Instant instant) {
        return instant.isAfter(this.instant)
                || instant.equals(this.instant) && isInclusive;
    }

    public boolean containsUpper(Instant instant) {
        return instant.isBefore(this.instant)
                || instant.equals(this.instant) && isInclusive;
    }

    @Override
    public String toString() {
        return instant.toString() + " " + (isInclusive ? "inclusive" : "exclusive");
    }
}
