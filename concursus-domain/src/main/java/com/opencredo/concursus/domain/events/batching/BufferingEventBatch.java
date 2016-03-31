package com.opencredo.concursus.domain.events.batching;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.channels.EventsOutChannel;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

final class BufferingEventBatch implements EventBatch {

    static EventBatch buffering(EventBatch eventBatch, EventsOutChannel outChannel) {
        return new BufferingEventBatch(eventBatch, outChannel);
    }

    private final EventBatch eventBatch;
    private final EventsOutChannel outChannel;
    private final List<Event> buffer = new LinkedList<>();

    private BufferingEventBatch(EventBatch eventBatch, EventsOutChannel outChannel) {
        this.eventBatch = eventBatch;
        this.outChannel = outChannel;
    }

    @Override
    public void accept(Event event) {
        eventBatch.accept(event);
        buffer.add(event);
    }

    @Override
    public UUID getId() {
        return eventBatch.getId();
    }

    @Override
    public void complete() {
        eventBatch.complete();
        outChannel.accept(buffer);
    }
}
