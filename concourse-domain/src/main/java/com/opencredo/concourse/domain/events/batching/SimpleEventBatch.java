package com.opencredo.concourse.domain.events.batching;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.writing.EventWriter;
import com.opencredo.concourse.domain.time.TimeUUID;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class SimpleEventBatch implements EventBatch {

    public static EventBatch writingTo(EventWriter eventWriter) {
        return new SimpleEventBatch(eventWriter);
    }

    private final UUID id = TimeUUID.timeBased();
    private final List<Event> events = new LinkedList<>();
    private final EventWriter eventWriter;

    private SimpleEventBatch(EventWriter eventWriter) {
        this.eventWriter = eventWriter;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void complete() {
        eventWriter.accept(events);
    }

    @Override
    public void accept(Event event) {
        events.add(event);
    }
}
