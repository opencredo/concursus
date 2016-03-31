package com.opencredo.concursus.domain.events.filtering.channel;

import com.opencredo.concursus.domain.events.channels.EventOutChannel;
import com.opencredo.concursus.domain.events.filtering.Filters;

import java.util.Arrays;
import java.util.function.UnaryOperator;

@FunctionalInterface
public interface EventOutChannelFilter extends UnaryOperator<EventOutChannel> {

    static EventOutChannelFilter compose(EventOutChannelFilter...filters) {
        return Filters.compose(Arrays.asList(filters))::apply;
    }

}
