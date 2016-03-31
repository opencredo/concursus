package com.opencredo.concursus.domain.events;

/**
 * Constant values for {@link Event} characteristics.
 */
public final class EventCharacteristics {

    private EventCharacteristics() {
    }

    /**
     * The characteristic the {@link Event} will have if it is an initial event, i.e. one that creates an aggregate.
     */
    public static final int IS_INITIAL = 1;

    /**
     * The characteristic the {@link Event} will have if it is a terminal event, i.e. one that completes the processing
     * of an aggregate.
     */
    public static final int IS_TERMINAL = 2;
}
