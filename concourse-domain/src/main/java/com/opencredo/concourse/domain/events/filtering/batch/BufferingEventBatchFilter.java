package com.opencredo.concourse.domain.events.filtering.batch;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.batching.EventBatch;
import com.opencredo.concourse.domain.events.channels.EventsOutChannel;

import java.util.LinkedList;
import java.util.List;

public final class BufferingEventBatchFilter implements EventBatchPostFilter {

    public static BufferingEventBatchFilter writingOnCompleteTo(EventsOutChannel outChannel) {
        return new BufferingEventBatchFilter(outChannel);
    }

    private final List<Event> buffer = new LinkedList<>();
    private final EventsOutChannel outChannel;

    private BufferingEventBatchFilter(EventsOutChannel outChannel) {
        this.outChannel = outChannel;
    }

    @Override
    public void afterComplete(EventBatch eventBatch) {
        outChannel.accept(buffer);
    }

    @Override
    public void afterAccept(EventBatch eventBatch, Event event) {
        buffer.add(event);
    }
}
