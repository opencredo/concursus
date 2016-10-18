package com.opencredo.concursus.examples;

import com.opencredo.concursus.data.tuples.TupleKey;
import com.opencredo.concursus.data.tuples.TupleSchema;
import com.opencredo.concursus.data.tuples.TupleSlot;
import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.common.VersionedName;
import com.opencredo.concursus.domain.events.Event;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;
import com.opencredo.concursus.mapping.events.methods.dispatching.DispatchingEventOutChannel;
import com.opencredo.concursus.mapping.events.methods.proxying.EventEmittingProxy;
import org.junit.Test;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class PackageExample {

    @HandlesEventsFor("parcel")
    public interface ParcelEvents {
        void receivedAtDepot(StreamTimestamp ts, String parcelId, String depotId);
        void loadedOntoTruck(StreamTimestamp ts, String parcelId, String truckId);
        void delivered(StreamTimestamp ts, String parcelId, String destinationId);
        void deliveryFailed(StreamTimestamp ts, String parcelId);
    }

    private static final TupleSchema receivedAtDepotSchema = TupleSchema.of("received at depot",
            TupleSlot.of("depotId", String.class));

    private static final TupleKey<String> depotId = receivedAtDepotSchema.getKey("depotId", String.class);

    public void writeEvents(Consumer<Event> eventConsumer) {
        Event packageReceived = Event.of(
                AggregateId.of("parcel", UUID.randomUUID().toString()),
                StreamTimestamp.now(),
                VersionedName.of("receivedAtDepot"),
                receivedAtDepotSchema.make(depotId.of("Lewisham")));

        eventConsumer.accept(packageReceived);
    }

    @Test
    public void streamExample() {
        Stream<String> strings = Stream.of("foo", "bar", "baz", "alpha", "beta", "gamma");
        strings.parallel().map(s -> s.length()).reduce(0, (a, b) -> a + b);
    }

    public void handleEvents() {
        Consumer<Event> handler = event -> {
            System.out.println("Package received at depot: " +
                event.getParameters().get(depotId));
        };

        writeEvents(handler);
    }

    public void java8WriteEvents(Consumer<Event> eventConsumer) {
        ParcelEvents parcelEvents = EventEmittingProxy.proxying(
                eventConsumer,
                ParcelEvents.class
        );

        parcelEvents.receivedAtDepot(
                StreamTimestamp.now(),
                UUID.randomUUID().toString(),
                "Lewisham"
        );
    }

    @HandlesEventsFor("parcel")
    public interface ReceivedAtDepotEvent {
        void receivedAtDepot(StreamTimestamp timestamp, String parcelId, String depotId);
    }

    public void java8HandleEvents() {
        Consumer<Event> handler = DispatchingEventOutChannel.toHandler(
                ReceivedAtDepotEvent.class,
                (timestamp, id, depotId) ->
                        System.out.println("Received at depot: " + depotId));

        writeEvents(handler);
    }

}
