package com.opencredo.concourse.domain.events.writing;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.logging.EventLog;
import com.opencredo.concourse.domain.events.publishing.EventPublisher;

import java.util.Collection;

public class PublishingEventWriter implements EventWriter {

    public static PublishingEventWriter using(EventLog eventLog, EventPublisher eventPublisher) {
        return new PublishingEventWriter(eventLog, eventPublisher);
    }

    private final EventLog eventLog;
    private final EventPublisher eventPublisher;

    private PublishingEventWriter(EventLog eventLog, EventPublisher eventPublisher) {
        this.eventLog = eventLog;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void accept(Collection<Event> events) {
        eventLog.apply(events).forEach(eventPublisher);
    }
}
