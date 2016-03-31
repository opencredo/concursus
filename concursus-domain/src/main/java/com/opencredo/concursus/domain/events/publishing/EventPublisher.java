package com.opencredo.concursus.domain.events.publishing;

import com.opencredo.concursus.domain.events.channels.EventOutChannel;

/**
 * An {@link EventOutChannel} that publishes events to event handlers after they have been logged.
 */
public interface EventPublisher extends EventOutChannel {
}
