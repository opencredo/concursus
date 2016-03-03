package com.opencredo.concourse.domain.events.filtering;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.publishing.EventPublisher;

import java.util.concurrent.ExecutorService;

public final class AsyncEventPublisherFilter implements EventPublisherIntercepter {

    public static AsyncEventPublisherFilter using(ExecutorService executorService) {
        return new AsyncEventPublisherFilter(executorService);
    }

    private final ExecutorService executorService;

    private AsyncEventPublisherFilter(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void onPublish(EventPublisher eventPublisher, Event event) {
        executorService.submit(() -> eventPublisher.accept(event));
    }

}
