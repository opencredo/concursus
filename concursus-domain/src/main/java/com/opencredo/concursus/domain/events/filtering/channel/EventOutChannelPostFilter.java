package com.opencredo.concursus.domain.events.filtering.channel;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.channels.EventOutChannel;

public interface EventOutChannelPostFilter extends EventOutChannelIntercepter {

    @Override
    default void onAccept(EventOutChannel outChannel, Event event) {
        outChannel.accept(event);
        afterAccept(outChannel, event);
    }

    void afterAccept(EventOutChannel outChannel, Event event);
}
