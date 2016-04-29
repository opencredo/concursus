package com.opencredo.concursus.spring.cassandra;

import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.events.methods.dispatching.DispatchingCachedEventSource;
import com.opencredo.concursus.mapping.events.methods.dispatching.DispatchingEventSourceFactory;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concursus.spring.cassandra.events.CassandraEventStoreBeans;
import com.opencredo.concursus.spring.events.EventSystemBeans;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { CassandraEventStoreBeans.class,  EventSystemBeans.class, TestConfiguration.class })
public class CassandraIntegrationTest {

    @ClassRule
    public static final CassandraCQLUnit CASSANDRA_CQL_UNIT =
            new CassandraCQLUnit(new ClassPathCQLDataSet("cql/tables.cql"), EmbeddedCassandraServerHelper.CASSANDRA_RNDPORT_YML_FILE);

    @BeforeClass
    public static void setUpCassandraServer() {
        System.setProperty("spring.data.cassandra.port",
                Integer.toString(CASSANDRA_CQL_UNIT.getCluster().getConfiguration().getProtocolOptions().getPort()));
    }

    @Autowired
    private ProxyingEventBus proxyingEventBus;

    @Autowired
    private DispatchingEventSourceFactory eventSourceDispatching;

    @Autowired
    private PersonEventHandler personEventHandler;

    @Test
    public void writeAndReadBatch() {
        String personId1 = "id1";
        String personId2 = "id2";
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

        final DispatchingCachedEventSource<PersonEvents> preloaded = eventSourceDispatching.dispatchingTo(PersonEvents.class)
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

        assertThat(personEventHandler.getPublishedEvents(), contains(
                "Arthur Putey was created with age 41",
                "age was changed to 42",
                "name was changed to Arthur Daley",
                "Arthur Dent was created with age 32",
                "name was changed to Arthur Danto"
        ));
    }

    private Function<Consumer<String>, PersonEvents> eventSummariser() {
        return caller -> new PersonEvents() {
            @Override
            public void created(StreamTimestamp timestamp, String personId, String name, int age) {
                caller.accept(name + " was created with age " + age);
            }

            @Override
            public void updatedAge(StreamTimestamp timestamp, String personId, int newAge) {
                caller.accept("age was changed to " + newAge);
            }

            @Override
            public void updatedName(StreamTimestamp timestamp, String personId, String newName) {
                caller.accept("name was changed to " + newName);
            }
        };
    }
}
