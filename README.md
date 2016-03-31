# Concursus

[![Maven Central](https://img.shields.io/maven-central/v/org.apache.maven/apache-maven.svg)](http://search.maven.org/#artifactdetails%7Ccom.opencredo%7Cconcursus%7C0.2%7Cpom)
[![Build Status](https://travis-ci.org/opencredo/concursus.svg?branch=master)](https://travis-ci.org/opencredo/concursus)

_Concursus_ is a Java 8 framework for building applications that use CQRS and event sourcing patterns, with a Cassandra event log implementation.

## Getting Started

Create a project with dependencies on `concursus-mapping`, `concursus-domain-json` and `jackson-datatype-jsr310`:

```xml
<dependency>
    <groupId>com.opencredo</groupId>
    <artifactId>concursus-mapping</artifactId>
</dependency>

<dependency>
    <groupId>com.opencredo</groupId>
    <artifactId>concursus-domain-json</artifactId>
</dependency>

<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

The first thing we might want to do is generate some events. To begin with, we need an interface class that defines the events we want to create:

```java
@HandlesEventsFor("person")
public interface Events {
    @Initial
    void created(StreamTimestamp ts, UUID personId, String name, LocalDate dateOfBirth);
    void changedName(StreamTimestamp ts, UUID personId, String newName);
    void movedToAddress(StreamTimestamp ts, UUID personId, UUID addressId);
    @Terminal
    void deleted(StreamTimestamp ts, UUID personId);
}
```

Each method in this interface defines an event which can occur to a person. We generate an event by calling one of these methods, which results in an event being sent to an `EventOutChannel`. Let's create a channel that simply prints the event to the console, and then create a proxy implementation of `PersonEvents` that sends events to this channel:

```java
// Create an EventOutChannel that simply prints events to the command line
EventOutChannel outChannel = System.out::println;

// Create a proxy that sends events to the outChannel.
PersonEvents proxy = EventEmittingProxy.proxying(outChannel, PersonEvents.class);

// Send an event via the proxy.
proxy.created(StreamTimestamp.now(), UUID.randomUUID(), "Arthur Putey", LocalDate.parse("1968-05-28"));
```

This will output a String like the following:

```
person:b2fb2f38-0473-4359-b62b-fad149caf2d5 created_0
at 2016-03-31T10:31:17.981Z/
with person/created_0{dateOfBirth=1968-05-28, name=Arthur Putey}
```

This means that an event of type `created_0` occurred to the object `person:b2fb2f38-0473-4359-b62b-fad149caf2d5` at `2016-03-31T10:31:17.981Z`, and this event had two parameters associated with it, `name` and `dateOfBirth`.

We can have the event encoded as JSON if we use an `EventOutChannel` that performs the encoding:

```java
// Create an EventOutChannel that formats events as JSON and sends them to a command line printer.
EventInChannel<String> print = System.out::println;
ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .configure(SerializationFeature.INDENT_OUTPUT, true);
EventOutChannel outChannel = JsonEventOutChannel.using(objectMapper, print);

// Create a proxy that sends events to the outChannel.
PersonEvents proxy = EventEmittingProxy.proxying(outChannel, PersonEvents.class);

// Send an event via the proxy.
proxy.created(StreamTimestamp.now(), UUID.randomUUID(), "Arthur Putey", LocalDate.parse("1968-05-28"));
```

This will output JSON like the following:

```json
{
  "aggregateType" : "person",
  "aggregateId" : "b449861a-0e8f-4012-bfe9-98a2a4f620f9",
  "name" : "created",
  "version" : "0",
  "eventTimestamp" : 1459420919667,
  "streamId" : "",
  "processingId" : "",
  "characteristics" : 1,
  "parameters" : {
    "dateOfBirth" : [ 1968, 5, 28 ],
    "name" : "Arthur Putey"
  }
}
```

Instead of simply printing things to the console, let's start storing events. We can use an `InMemoryEventStore` to begin with:

```java
// Create an InMemoryEventStore, and a proxy that sends events to it.
InMemoryEventStore eventStore = InMemoryEventStore.empty();
PersonEvents proxy = EventEmittingProxy.proxying(eventStore.toEventOutChannel(), PersonEvents.class);

// Send an event via the proxy.
final UUID personId = UUID.randomUUID();
proxy.created(StreamTimestamp.now(), personId, "Arthur Putey", LocalDate.parse("1968-05-28"));

// Create an EventTypeMatcher based on the Events interface, and use it to map events back out of the store
EventTypeMatcher typeMatcher = EmitterInterfaceInfo.forInterface(PersonEvents.class).getEventTypeMatcher();
// Retrieve the stored events for the aggregate with id=person/personId, and print them to the console.
EventSource.retrievingWith(eventStore)
        .getEvents(typeMatcher, AggregateId.of("person", personId))
        .forEach(System.out::println);
```

Once we have stored events, we can replay them to event handlers, mapping them back into method calls on the `PersonEvents` interface:

```
// Create a mock handler for person events.
PersonEvents handler = mock(PersonEvents.class);

// Create an InMemoryEventStore, and a proxy that sends events to it.
InMemoryEventStore eventStore = InMemoryEventStore.empty();
PersonEvents proxy = EventEmittingProxy.proxying(eventStore.toEventOutChannel(), PersonEvents.class);

// Send an event via the proxy.
UUID personId = UUID.randomUUID();
proxy.created(StreamTimestamp.now(), personId, "Arthur Putey", LocalDate.parse("1968-05-28"));

// Replay the stored events for the person with id=person/personId to the handler instance.
DispatchingEventSource.dispatching(EventSource.retrievingWith(eventStore), PersonEvents.class)
        .replaying(personId)
        .replayAll(handler);

// Verify that the handler received the event.
verify(handler).created(any(StreamTimestamp.class), any(UUID.class), eq("Arthur Putey"), eq(LocalDate.parse("1968-05-28")));
```

Check out the [Examples](https://github.com/opencredo/concursus/tree/master/concursus-examples/src/test/java/com/opencredo/concursus/examples) for more detailed examples, including command processing and state-building.

## Using Cassandra and Redis

Eventually you will want to store events more permanently. Cassandra and Redis event store implementations are provided in `concursus-cassandra` and `concursus-redis` respectively. You will need to create a suitable keyspace and tables in Cassandra before you can use it:

```cql
CREATE KEYSPACE IF NOT EXISTS concursus
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 2 };

CREATE TABLE IF NOT EXISTS concursus.Event (
   aggregateType text,
   aggregateId uuid,
   eventTimestamp timestamp,
   streamId text,
   processingId timeuuid,
   name text,
   version text,
   parameters map<text, text>,
   characteristics int,
   PRIMARY KEY((aggregateType, aggregateId), eventTimestamp, streamId)
) WITH CLUSTERING ORDER BY (eventTimestamp DESC);

CREATE TABLE IF NOT EXISTS concursus.Catalogue (
    aggregateType text,
    bucket int,
    aggregateId uuid,
    PRIMARY KEY ((aggregateType, bucket), aggregateId)
) WITH CLUSTERING ORDER BY (aggregateId DESC);
```