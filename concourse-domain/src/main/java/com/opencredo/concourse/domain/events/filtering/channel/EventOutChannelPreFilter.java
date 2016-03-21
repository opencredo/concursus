package com.opencredo.concourse.domain.events.filtering.channel;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.channels.EventOutChannel;

public interface EventOutChannelPreFilter extends EventOutChannelIntercepter {

    @Override
    default void onAccept(EventOutChannel outChannel, Event event) {
        if (beforeAccept(outChannel, event)) {
            outChannel.accept(event);
        }
    }

    boolean beforeAccept(EventOutChannel outChannel, Event event);
}
