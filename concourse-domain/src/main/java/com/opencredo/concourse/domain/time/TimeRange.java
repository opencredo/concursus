package com.opencredo.concourse.domain.time;

import java.time.Instant;
import java.util.Optional;

public final class TimeRange {

    private static final TimeRange UNBOUNDED = new TimeRange(Optional.empty(), Optional.empty());

    public static TimeRange unbounded() {
        return UNBOUNDED;
    }

    public interface UpperBoundCapture {
        TimeRange to(Optional<TimeRangeBound> upper);
        default TimeRange toInclusive(Instant upper) {
            return to(Optional.of(TimeRangeBound.inclusive(upper)));
        }
        default TimeRange toExclusive(Instant upper) {
            return to(Optional.of(TimeRangeBound.exclusive(upper)));
        }
        default TimeRange toUnbounded() {
            return to(Optional.empty());
        }
    }

    public static UpperBoundCapture from(Optional<TimeRangeBound> lower) {
        return upper -> new TimeRange(lower, upper);
    }

    public static UpperBoundCapture fromInclusive(Instant lower) {
        return from(Optional.of(TimeRangeBound.inclusive(lower)));
    }

    public static UpperBoundCapture fromExclusive(Instant upper) {
        return from(Optional.of(TimeRangeBound.exclusive(upper)));
    }

    public static UpperBoundCapture fromUnbounded() {
        return from(Optional.empty());
    }

    private final Optional<TimeRangeBound> lowerBound;
    private final Optional<TimeRangeBound> upperBound;

    private TimeRange(Optional<TimeRangeBound> lowerBound, Optional<TimeRangeBound> upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public boolean contains(Instant instant) {
        return lowerBound.map(lower -> lower.containsLower(instant)).orElse(true)
                && upperBound.map(upper -> upper.containsUpper(instant)).orElse(true);
    }

    public boolean isUnbounded() {
        return !lowerBound.isPresent() && !upperBound.isPresent();
    }

    public Optional<TimeRangeBound> getLowerBound() {
        return lowerBound;
    }

    public Optional<TimeRangeBound> getUpperBound() {
        return upperBound;
    }
}
