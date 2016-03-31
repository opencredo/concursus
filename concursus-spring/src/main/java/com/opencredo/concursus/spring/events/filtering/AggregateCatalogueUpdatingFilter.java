package com.opencredo.concursus.spring.events.filtering;

import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.cataloguing.AggregateCatalogue;
import com.opencredo.concursus.domain.events.filtering.log.EventLogPostFilter;
import com.opencredo.concursus.domain.events.logging.EventLog;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

public final class AggregateCatalogueUpdatingFilter implements EventLogPostFilter {

    private final AggregateCatalogue aggregateCatalogue;

    @Autowired
    public AggregateCatalogueUpdatingFilter(AggregateCatalogue aggregateCatalogue) {
        this.aggregateCatalogue = aggregateCatalogue;
    }

    @Override
    public Collection<Event> afterLog(EventLog eventLog, Collection<Event> events) {
        events.forEach(aggregateCatalogue);
        return events;
    }
}
