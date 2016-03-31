package com.opencredo.concursus.domain.events.filtering.channel;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.channels.EventOutChannel;

@FunctionalInterface
public interface ErrorRecoveringEventOutChannelFilter extends EventOutChannelIntercepter {

    @Override
    default void onAccept(EventOutChannel outChannel, Event event) {
        try {
            outChannel.accept(event);
        } catch (Exception e) {
            recover(outChannel, event, e);
        }
    }

    void recover(EventOutChannel outChannel, Event event, Exception e);
}
