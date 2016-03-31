package com.opencredo.concursus.domain.events.processing;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.channels.EventsOutChannel;
import com.opencredo.concursus.domain.events.logging.EventLog;

/**
 * Processes the {@link Event}s collected in an {@link com.opencredo.concursus.domain.events.batching.EventBatch}
 * on batch completion.
 */
public interface EventBatchProcessor extends EventsOutChannel {

    /**
     * Create an {@link EventBatchProcessor} that forwards the events to the supplied {@link EventsOutChannel}
     * @param outChannel The {@link EventsOutChannel} to forward events to.
     * @return The constructed {@link EventBatchProcessor}.
     */
    static EventBatchProcessor forwardingTo(EventsOutChannel outChannel) {
        return outChannel::accept;
    }

    /**
     * Create an {@link EventBatchProcessor} that logs the events with the supplied {@link EventLog}
     * @param eventLog The {@link EventLog} to log the events with.
     * @return The constructed {@link EventBatchProcessor}.
     */
    static EventBatchProcessor loggingWith(EventLog eventLog) {
        return eventLog::apply;
    }
}
