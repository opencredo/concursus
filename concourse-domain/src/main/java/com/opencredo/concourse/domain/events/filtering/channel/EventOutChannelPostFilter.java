package com.opencredo.concourse.domain.events.filtering.channel;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.channels.EventOutChannel;

public interface EventOutChannelPostFilter extends EventOutChannelIntercepter {

    @Override
    default void onAccept(EventOutChannel outChannel, Event event) {
        outChannel.accept(event);
        afterAccept(outChannel, event);
    }

    void afterAccept(EventOutChannel outChannel, Event event);
}
