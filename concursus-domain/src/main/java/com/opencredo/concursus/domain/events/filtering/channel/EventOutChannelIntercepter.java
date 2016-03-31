package com.opencredo.concursus.domain.events.filtering.channel;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.channels.EventOutChannel;

public interface EventOutChannelIntercepter extends EventOutChannelFilter {

    @Override
    default EventOutChannel apply(EventOutChannel outChannel) {
        return event -> onAccept(outChannel, event);
    }

    void onAccept(EventOutChannel outChannel, Event event);

}
