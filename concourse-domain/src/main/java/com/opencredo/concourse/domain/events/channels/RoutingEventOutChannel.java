package com.opencredo.concourse.domain.events.channels;

import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.events.Event;

import java.util.Map;

public final class RoutingEventOutChannel implements EventOutChannel {

    public static RoutingEventOutChannel routingWith(Map<AggregateId, EventOutChannel> channelsById) {
        return new RoutingEventOutChannel(channelsById);
    }

    private final Map<AggregateId, EventOutChannel> channelsById;

    private RoutingEventOutChannel(Map<AggregateId, EventOutChannel> channelsById) {
        this.channelsById = channelsById;
    }

    @Override
    public void accept(Event event) {
        EventOutChannel outChannel = channelsById.get(event.getAggregateId());

        if (outChannel != null) {
            outChannel.accept(event);
        }
    }
}
