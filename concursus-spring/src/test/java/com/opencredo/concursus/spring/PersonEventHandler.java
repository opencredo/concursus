package com.opencredo.concursus.spring;

import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.spring.events.publishing.EventHandler;

import java.util.ArrayList;
import java.util.List;

@EventHandler
public class PersonEventHandler implements PersonEvents {

    private final List<String> publishedEvents = new ArrayList<>();

    public List<String> getPublishedEvents() {
        return publishedEvents;
    }

    @Override
    public void created(StreamTimestamp timestamp, String personId, String name, int age) {
        publishedEvents.add(name + " was created with age " + age);
    }

    @Override
    public void updatedAge(StreamTimestamp timestamp, String personId, int newAge) {
        publishedEvents.add("age was changed to " + newAge);
    }

    @Override
    public void updatedName(StreamTimestamp timestamp, String personId, String newName) {
        publishedEvents.add("name was changed to " + newName);
    }
}
