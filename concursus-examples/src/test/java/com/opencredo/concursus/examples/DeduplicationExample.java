package com.opencredo.concursus.examples;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.events.EventIdentity;
import com.opencredo.concursus.domain.events.dispatching.EventBus;
import com.opencredo.concursus.domain.events.filtering.log.EventLogPreFilter;
import com.opencredo.concursus.domain.events.logging.EventLog;
import com.opencredo.concursus.domain.events.processing.EventBatchProcessor;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;
import org.junit.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class DeduplicationExample {

    public static final class DeduplicatingFilter implements EventLogPreFilter {

        public static EventLogPreFilter expiringAfter(int duration, TimeUnit unit) {
            return new DeduplicatingFilter(CacheBuilder.<EventIdentity, Boolean>newBuilder()
                    .expireAfterWrite(duration, unit)
                    .build());
        }

        private final Cache<EventIdentity, Boolean> observedEvents;

        public DeduplicatingFilter(Cache<EventIdentity, Boolean> observedTimestamps) {
            this.observedEvents = observedTimestamps;
        }

        @Override
        public Collection<Event> beforeLog(EventLog eventLog, Collection<Event> events) {
            // Throws away all duplicate events
            return events.stream()
                    .filter(this::isNewEvent)
                    .collect(Collectors.toList());
        }

        private boolean isNewEvent(Event event) {
            return observedEvents.asMap().putIfAbsent(event.getIdentity(), true) == null;
        }
    }

    @Test
    public void detectsAndRemovesDuplicatesWithinGivenTimePeriod() {
        // Create an event log that writes events straight into a List
        List<Event> processedEvents = new ArrayList<>();
        EventLog eventLog = EventLog.loggingTo(processedEvents::addAll);

        // Filter the event log with a de-duplicating filter
        EventLog filteredEventLog = DeduplicatingFilter.expiringAfter(5, TimeUnit.SECONDS).apply(eventLog);

        // Create an event bus writing to the deduplicated log
        EventBus eventBus = EventBus.processingWith(EventBatchProcessor.loggingWith(filteredEventLog));
        ProxyingEventBus proxyingEventBus = ProxyingEventBus.proxying(eventBus);

        // Write some events
        StreamTimestamp ts = StreamTimestamp.now();
        UUID personId = UUID.randomUUID();
        proxyingEventBus.dispatch(Person.Events.class, personEvents -> {
            personEvents.created(ts, personId, "Arthur Putey", LocalDate.parse("1968-05-28"));
            personEvents.changedName(ts.plus(1, ChronoUnit.MILLIS), personId, "Arthur Daley");
            // Duplicate timestamp
            personEvents.changedName(ts.plus(1, ChronoUnit.MILLIS), personId, "Arthur Mumby");
        });

        // Only two events were written, and the first write with the duplicate timestamp succeeded
        assertThat(processedEvents, hasSize(2));
        assertThat(processedEvents.get(1).getParameters().get("newName"), equalTo("Arthur Daley"));
    }
}
