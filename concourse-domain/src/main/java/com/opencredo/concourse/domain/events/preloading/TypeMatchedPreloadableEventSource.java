package com.opencredo.concourse.domain.events.preloading;

import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.time.TimeRange;

import java.util.Collection;
import java.util.UUID;

public interface TypeMatchedPreloadableEventSource extends EventSource {

    EventSource preload(String aggregateType, Collection<UUID> aggregateIds, TimeRange timeRange);

    default EventSource preload(String aggregateType, Collection<UUID> aggregateIds) {
        return preload(aggregateType, aggregateIds, TimeRange.unbounded());
    }

}
