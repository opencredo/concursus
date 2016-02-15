package com.opencredo.concourse.domain.events.sourcing;

import com.opencredo.concourse.domain.AggregateId;

@FunctionalInterface
public interface EventSource {

    EventHistory getEventHistory(AggregateId aggregateId);

}
