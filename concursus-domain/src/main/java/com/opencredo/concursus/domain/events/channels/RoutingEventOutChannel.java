package com.opencredo.concursus.domain.events.channels;

import com.opencredo.concursus.domain.events.Event;

import java.util.Map;

public final class RoutingEventOutChannel implements EventOutChannel {

    public static RoutingEventOutChannel routingWith(Map<String, EventOutChannel> channelsById) {
        return new RoutingEventOutChannel(channelsById);
    }

    private final Map<String, EventOutChannel> channelsById;

    private RoutingEventOutChannel(Map<String, EventOutChannel> channelsById) {
        this.channelsById = channelsById;
    }

    @Override
    public void accept(Event event) {
        EventOutChannel outChannel = channelsById.get(event.getAggregateId().getId());

        if (outChannel != null) {
            outChannel.accept(event);
        }
    }
}
