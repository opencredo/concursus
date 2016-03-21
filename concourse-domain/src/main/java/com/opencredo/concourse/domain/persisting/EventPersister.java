package com.opencredo.concourse.domain.persisting;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.channels.EventsOutChannel;

/**
 * An {@link EventsOutChannel} that writes {@link Event}s into a persistent store.
 */
public interface EventPersister extends EventsOutChannel {
}
