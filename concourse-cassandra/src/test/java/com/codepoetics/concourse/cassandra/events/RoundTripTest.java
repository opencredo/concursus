package com.codepoetics.concourse.cassandra.events;

import com.datastax.driver.core.Cluster;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concourse.domain.events.batching.SimpleEventBatch;
import com.opencredo.concourse.domain.events.caching.CachingEventSource;
import com.opencredo.concourse.domain.events.dispatching.EventBus;
import com.opencredo.concourse.domain.events.logging.EventLog;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.events.writing.EventWriter;
import com.opencredo.concourse.domain.events.writing.PublishingEventWriter;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.events.methods.dispatching.DispatchingEventSourceFactory;
import com.opencredo.concourse.mapping.events.methods.dispatching.DispatchingCachedEventSource;
import com.opencredo.concourse.mapping.events.methods.proxying.ProxyingEventBus;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.*;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class RoundTripTest {

    @ClassRule
    public static final CassandraCQLUnit CASSANDRA_CQL_UNIT =
            new CassandraCQLUnit(new ClassPathCQLDataSet("cql/tables.cql"), EmbeddedCassandraServerHelper.CASSANDRA_RNDPORT_YML_FILE);

    @BeforeClass
    public static void setUpCassandraServer() {
        System.setProperty("spring.data.cassandra.port",
                Integer.toString(CASSANDRA_CQL_UNIT.getCluster().getConfiguration().getProtocolOptions().getPort()));
    }

    @Rule
    public ContiPerfRule contiPerfRule = new ContiPerfRule();

    private final Cluster cluster = CASSANDRA_CQL_UNIT.getCluster();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final EventLog cassandraEventLog = CassandraEventLog.create(
            new CassandraTemplate(cluster.connect("Concourse")),
            JsonSerialiser.using(objectMapper));

    private final EventWriter eventWriter = PublishingEventWriter.using(cassandraEventLog, event -> {});

    private final CassandraEventRetriever cassandraEventRetriever = CassandraEventRetriever.create(
            new CassandraTemplate(cluster.connect("Concourse")),
            JsonDeserialiser.using(objectMapper));

    private final EventSource eventSource = CachingEventSource.retrievingWith(cassandraEventRetriever);
    private final DispatchingEventSourceFactory eventSourceDispatching = DispatchingEventSourceFactory.dispatching(eventSource);

    private final EventBus eventBus = () -> SimpleEventBatch.writingTo(eventWriter);

    private final ProxyingEventBus proxyingEventBus = ProxyingEventBus.proxying(eventBus);

    @HandlesEventsFor("person")
    public interface PersonEvents {
        void created(StreamTimestamp timestamp, UUID personId, String name, int age);
        void updatedAge(StreamTimestamp timestamp, UUID personId, int newAge);
        void updatedName(StreamTimestamp timestamp, UUID personId, String newName);
    }

    @Test
    public void writeAndReadBatch() {
        UUID personId1 = UUID.randomUUID();
        UUID personId2 = UUID.randomUUID();
        Instant start = Instant.now();

        proxyingEventBus.dispatch(PersonEvents.class, batch -> {
            batch.created(
                    StreamTimestamp.of("test", start),
                    personId1,
                    "Arthur Putey",
                    41);

            batch.updatedAge(
                    StreamTimestamp.of("test", start.plusMillis(1)),
                    personId1,
                    42);

            batch.updatedName(
                    StreamTimestamp.of("test", start.plusMillis(2)),
                    personId1,
                    "Arthur Daley");

            batch.created(
                    StreamTimestamp.of("test", start),
                    personId2,
                    "Arthur Dent",
                    32);

            batch.updatedName(
                    StreamTimestamp.of("test", start.plusMillis(1)),
                    personId2,
                    "Arthur Danto"
            );
        });

        final DispatchingCachedEventSource<PersonEvents> preloaded = eventSourceDispatching.to(PersonEvents.class)
                .preload(personId1, personId2);

        List<String> personHistory1 = preloaded.replaying(personId1).collectAll(eventSummariser());
        List<String> personHistory2 = preloaded.replaying(personId2).inAscendingOrder().collectAll(eventSummariser());

        assertThat(personHistory1, contains(
                "name was changed to Arthur Daley",
                "age was changed to 42",
                "Arthur Putey was created with age 41"
        ));

        assertThat(personHistory2, contains(
                "Arthur Dent was created with age 32",
                "name was changed to Arthur Danto"
        ));
    }

    private Function<Consumer<String>, PersonEvents> eventSummariser() {
        return caller -> new PersonEvents() {
            @Override
            public void created(StreamTimestamp timestamp, UUID personId, String name, int age) {
                caller.accept(name + " was created with age " + age);
            }

            @Override
            public void updatedAge(StreamTimestamp timestamp, UUID personId, int newAge) {
                caller.accept("age was changed to " + newAge);
            }

            @Override
            public void updatedName(StreamTimestamp timestamp, UUID personId, String newName) {
                caller.accept("name was changed to " + newName);
            }
        };
    }

    @Ignore
    @PerfTest(invocations = 100, warmUp = 5000)
    @Test
    public void writeAThousandEvents() {
        PersonEvents dispatcher = proxyingEventBus.getDispatcherFor(PersonEvents.class);
        for (int i = 0; i < 1000; i++) {
            dispatcher.created(StreamTimestamp.of("test", Instant.now()), UUID.randomUUID(), "Arthur Mumby", 41);
        }
    }
}
