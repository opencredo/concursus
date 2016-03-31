package com.opencredo.concursus.domain.time;

import java.time.Instant;
import java.util.Optional;

/**
 * A range of {@link Instant}s
 */
public final class TimeRange {

    private static final TimeRange UNBOUNDED = new TimeRange(Optional.empty(), Optional.empty());

    /**
     * Returns an unbounded time range.
     * @return An unbounded time range.
     */
    public static TimeRange unbounded() {
        return UNBOUNDED;
    }

    /**
     * Captures the upper bound of a time range for which the lower bound is already known, and returns the complete time range.
     */
    public interface UpperBoundCapture {
        /**
         * Returns the time range up to the given upper bound.
         * @param upper The upper bound.
         * @return The complete time range.
         */
        TimeRange to(Optional<TimeRangeBound> upper);

        /**
         * Returns the time range up to the given instant, inclusively.
         * @param upper The inclusive upper bound.
         * @return The complete time range.
         */
        default TimeRange toInclusive(Instant upper) {
            return to(Optional.of(TimeRangeBound.inclusive(upper)));
        }

        /**
         * Returns the time range up to the given instant, exclusively.
         * @param upper The exclusive upper bound.
         * @return The complete time range.
         */
        default TimeRange toExclusive(Instant upper) {
            return to(Optional.of(TimeRangeBound.exclusive(upper)));
        }

        /**
         * Returns a time range with no upper bound.
         * @return The complete time range.
         */
        default TimeRange toUnbounded() {
            return to(Optional.empty());
        }
    }

    /**
     * Start a time range from the given lower bound.
     * @param lower The lower bound of the time range.
     * @return Object which captures the upper bound of the time range and returns the complete time range.
     */
    public static UpperBoundCapture from(Optional<TimeRangeBound> lower) {
        return upper -> new TimeRange(lower, upper);
    }

    /**
     * Start a time range from the given lower instant, inclusively.
     * @param lower The inclusive lower bound.
     * @return Object which captures the upper bound of the time range and returns the complete time range.
     */
    public static UpperBoundCapture fromInclusive(Instant lower) {
        return from(Optional.of(TimeRangeBound.inclusive(lower)));
    }

    /**
     * Start a time range from the given lower instant, exclusively.
     * @param lower The inclusive lower bound.
     * @return Object which captures the upper bound of the time range and returns the complete time range.
     */
    public static UpperBoundCapture fromExclusive(Instant lower) {
        return from(Optional.of(TimeRangeBound.exclusive(lower)));
    }

    /**
     * Start a time range with no lower bound.
     * @return Object which captures the upper bound of the time range and returns the complete time range.
     */
    public static UpperBoundCapture fromUnbounded() {
        return from(Optional.empty());
    }

    private final Optional<TimeRangeBound> lowerBound;
    private final Optional<TimeRangeBound> upperBound;

    private TimeRange(Optional<TimeRangeBound> lowerBound, Optional<TimeRangeBound> upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    /**
     * Test if the time range contains the given instant.
     * @param instant The {@link Instant} to test.
     * @return True if the time range contains the given instant, false otherwise.
     */
    public boolean contains(Instant instant) {
        return lowerBound.map(lower -> lower.containsLower(instant)).orElse(true)
                && upperBound.map(upper -> upper.containsUpper(instant)).orElse(true);
    }

    /**
     * Test if the time range is completely unbounded.
     * @return True if the time range is completely unbounded, false otherwise.
     */
    public boolean isUnbounded() {
        return !lowerBound.isPresent() && !upperBound.isPresent();
    }

    /**
     * Get the lower bound of the time range, if present.
     * @return The lower bound of the time range, if present.
     */
    public Optional<TimeRangeBound> getLowerBound() {
        return lowerBound;
    }

    /**
     * Get the upper bound of the time range, if present.
     * @return The upper bound of the time range, if present.
     */
    public Optional<TimeRangeBound> getUpperBound() {
        return upperBound;
    }

    @Override
    public String toString() {
        return "TimeRange from " + lowerBound.map(TimeRangeBound::toString).orElse("unbounded") + " to "
                + upperBound.map(TimeRangeBound::toString).orElse("unbounded");
    }
}
