package com.opencredo.concourse.domain.events.filtering.channel;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.channels.EventOutChannel;

import java.util.concurrent.ExecutorService;

public final class AsyncEventOutChannelFilter implements EventOutChannelIntercepter {

    public static AsyncEventOutChannelFilter using(ExecutorService executorService) {
        return new AsyncEventOutChannelFilter(executorService);
    }

    private final ExecutorService executorService;

    private AsyncEventOutChannelFilter(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void onAccept(EventOutChannel outChannel, Event event) {
        executorService.submit(() -> outChannel.accept(event));
    }

}
