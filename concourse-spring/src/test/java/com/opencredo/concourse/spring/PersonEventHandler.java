package com.opencredo.concourse.spring;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.spring.events.publishing.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EventHandler
public class PersonEventHandler implements PersonEvents {

    private final List<String> publishedEvents = new ArrayList<>();

    public List<String> getPublishedEvents() {
        return publishedEvents;
    }

    @Override
    public void created(StreamTimestamp timestamp, UUID personId, String name, int age) {
        publishedEvents.add(name + " was created with age " + age);
    }

    @Override
    public void updatedAge(StreamTimestamp timestamp, UUID personId, int newAge) {
        publishedEvents.add("age was changed to " + newAge);
    }

    @Override
    public void updatedName(StreamTimestamp timestamp, UUID personId, String newName) {
        publishedEvents.add("name was changed to " + newName);
    }
}
