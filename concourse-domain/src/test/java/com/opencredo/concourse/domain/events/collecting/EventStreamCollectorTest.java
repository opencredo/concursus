package com.opencredo.concourse.domain.events.collecting;

import org.junit.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static com.opencredo.concourse.domain.events.collecting.EventStreamCollector.toNewState;
import static com.opencredo.concourse.domain.events.collecting.EventStreamCollector.toState;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

public class EventStreamCollectorTest {

    @Test
    public void convertsStreamOfEventsIntoFinalState() {
        Stream<String> events = Stream.of(
                "create:red",
                "append:dish",
                "uppercase",
                "append:ness"
        );

        assertThat(events.collect(toState(
                EventStreamCollectorTest::initialise,
                EventStreamCollectorTest::update)),
        equalTo(Optional.of("REDDISHness")));
    }

    @Test
    public void appliesLaterEventsToSnapshot() {
        Stream<String> events = Stream.of(
                "uppercase",
                "append:zzy",
                "lowercase"
        );

        assertThat(events.collect(toNewState("xy", EventStreamCollectorTest::update)),
                equalTo("xyzzy"));
    }

    private static String initialise(String event) {
        assertTrue(event.startsWith("create:"));
        return event.substring(7);
    }

    private static String update(String event, String state) {
        if (event.equals("uppercase")) {
            return state.toUpperCase();
        }

        if (event.equals("lowercase")) {
            return state.toLowerCase();
        }

        if (event.startsWith("append:")) {
            return state + event.substring(7);
        }

        throw new UnsupportedOperationException("Event " + event + " not handled");
    }
}
