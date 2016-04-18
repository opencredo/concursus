package com.opencredo.concursus.domain.time;

import java.time.Instant;

/**
 * The upper or lower bound of a {@link TimeRange}.
 */
public final class TimeRangeBound {

    /**
     * Create an inclusive bound.
     * @param instant The inclusive bound value.
     * @return The constructed {@link TimeRangeBound}.
     */
    public static TimeRangeBound inclusive(Instant instant) {
        return new TimeRangeBound(instant, true);
    }

    /**
     * Create an exclusive bound.
     * @param instant The exclusive bound value.
     * @return The constructed {@link TimeRangeBound}.
     */
    public static TimeRangeBound exclusive(Instant instant) {
        return new TimeRangeBound(instant, false);
    }

    private final Instant instant;
    private final boolean isInclusive;

    private TimeRangeBound(Instant instant, boolean isInclusive) {
        this.instant = instant;
        this.isInclusive = isInclusive;
    }

    /**
     * Get the bound {@link Instant}.
     * @return The bound {@link Instant}.
     */
    public Instant getInstant() {
        return instant;
    }

    /**
     * Is the bound inclusive?
     * @return True if the bound is inclusive, false if it is exclusive.
     */
    public boolean isInclusive() {
        return isInclusive;
    }

    /**
     * Is the supplied {@link Instant} within the lower bound represented by this object?
     * @param instant The {@link Instant} to test.
     * @return True of the supplied {@link Instant} is within the lower bound represented by this object, false otherwise.
     */
    public boolean containsLower(Instant instant) {
        return instant.isAfter(this.instant)
                || instant.equals(this.instant) && isInclusive;
    }


    /**
     * Is the supplied {@link Instant} within the upper bound represented by this object?
     * @param instant The {@link Instant} to test.
     * @return True of the supplied {@link Instant} is within the upper bound represented by this object, false otherwise.
     */
    public boolean containsUpper(Instant instant) {
        return instant.isBefore(this.instant)
                || instant.equals(this.instant) && isInclusive;
    }

    @Override
    public String toString() {
        return instant.toString() + " " + (isInclusive ? "inclusive" : "exclusive");
    }
}
