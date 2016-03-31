package com.opencredo.concourse.domain.events.channels;

import com.opencredo.concourse.domain.events.Event;

import java.util.Map;
import java.util.UUID;

public final class RoutingEventOutChannel implements EventOutChannel {

    public static RoutingEventOutChannel routingWith(Map<UUID, EventOutChannel> channelsById) {
        return new RoutingEventOutChannel(channelsById);
    }

    private final Map<UUID, EventOutChannel> channelsById;

    private RoutingEventOutChannel(Map<UUID, EventOutChannel> channelsById) {
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
