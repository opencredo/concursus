package com.opencredo.concursus.domain.events.persisting;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.channels.EventsOutChannel;

/**
 * An {@link EventsOutChannel} that writes {@link Event}s into a persistent store.
 */
public interface EventPersister extends EventsOutChannel {
}
