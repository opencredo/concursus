package com.opencredo.concourse.domain.events.filtering.channel;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.channels.EventOutChannel;

public interface EventOutChannelIntercepter extends EventOutChannelFilter {

    @Override
    default EventOutChannel apply(EventOutChannel outChannel) {
        return event -> onAccept(outChannel, event);
    }

    void onAccept(EventOutChannel outChannel, Event event);

}
