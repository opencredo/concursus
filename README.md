# concourse

_Concourse_ is a Java 8 framework for building applications that use CQRS and event sourcing patterns, with a Cassandra event log implementation.

Events are defined using methods on Java interfaces, like so:

```java
@HandlesEventsFor("person")
public interface PersonEvents {

    void created(StreamTimestamp ts, UUID personId, String name, LocalDate dateOfBirth);
    void changedName(StreamTimestamp ts, UUID personId, String newName);
    void phoneNumberAdded(StreamTimestamp ts, UUID personId, UUID phoneNumberId, String phoneNumber, String description);
  
    void phoneNumberChanged(StreamTimestamp ts, UUID personId, UUID phoneNumberId, String newPhoneNumber);
    
    @Name(name="phoneNumberChanged", version="2")
    void phoneNumberChanged(StreamTimestamp ts, UUID personId, UUID phoneNumberId, String newPhoneNumber, String newDescription);
    
    void phoneNumberRemoved(StreamTimestamp ts, UUID personId, UUID phoneNumberId);
    void deleted(StreamTimestamp ts, UUID personId);
    
}
```

We send events to an `EventBus` by getting a dispatcher for the desired interface:

```java
eventBus.dispatch(PersonEvents.class, events ->
    events.phoneNumberChanged(ts, personId, phoneNumberId, "0898505050", "work phone"));
```

Events are recorded in the event log and then published to event handlers which can update query-optimised views or propagate integration events out to the wider system.

To retrieve the state of an entity, we replay its event history, typically to an object that builds up a picture of its current state:

```java
@HandlesEventsFor("person")
public final class PersonState {

    @HandlesEvent
    public static PersonState created(UUID id, String name, LocalDate dateOfBirth) {
        return new PersonState(id, name, dateOfBirth);
    }
    
    private final UUID id;
    private String name;
    private final LocalDate dateOfBirth;
    private final Map<UUID, String[]> phoneNumbers = new HashMap<>();
    private boolean isDeleted = false;
    
    private PersonState(UUID id, String name, LocalDate dateOfBirth) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
    }
    
    @HandlesEvent
    public void changedName(String newName) {
        name = newName;
    }
    
    @HandlesEvent
    public void phoneNumberAdded(UUID phoneNumberId, String phoneNumber, String description) {
        phoneNumbers.put(phoneNumberId, new String[] { phoneNumber, description });
    }
    
    @HandlesEvent
    public void phoneNumberChanged(UUID phoneNumberId, String newPhoneNumber) {
        phoneNumbers.computeIfAbsent(phoneNumberId, id -> new String[] { "", "" })[0] = newPhoneNumber;
    }
    
    @HandlesEvent(name="phoneNumberChanged", version="2")
    public void phoneNumberChanged(UUID phoneNumberId, String newPhoneNumber, String newDescription) {
        String[] phoneNumberDetails = phoneNumbers.computeIfAbsent(phoneNumberId, id -> new String[2]);
        phoneNumberDetails[0] = newPhoneNumber;
        phoneNumberDetails[1] = newDescription;
    }
    
    @HandlesEvent
    public void phoneNumberRemoved(UUID phoneNumberId) {
        phoneNumbers.remove(phoneNumberId);
    }
    
    @HandlesEvent
    public void deleted() {
        isDeleted = true;
    }
    
    // getters go here
}

Optional<PersonState> personState = StateBuilder.forStateClass(PersonState.class)
    .buildState(eventSource, personId)
    .filter(p -> !p.isDeleted());
```
