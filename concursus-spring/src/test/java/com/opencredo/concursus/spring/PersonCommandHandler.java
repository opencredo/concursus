package com.opencredo.concursus.spring;

import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concursus.spring.commands.processing.CommandHandler;
import org.springframework.beans.factory.annotation.Autowired;

@CommandHandler
public class PersonCommandHandler implements PersonCommands {

    private final ProxyingEventBus eventBus;

    @Autowired
    public PersonCommandHandler(ProxyingEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void create(StreamTimestamp ts, String personId, String name, int age) {
        eventBus.getDispatcherFor(PersonEvents.class).created(ts, personId, name, age);
    }

    @Override
    public void updateNameAndAge(StreamTimestamp ts, String personId, String newName, int newAge) {
        eventBus.dispatch(PersonEvents.class, events -> {
            events.updatedAge(ts.subStream("age"), personId, newAge);
            events.updatedName(ts.subStream("name"), personId, newName);
        });
    }
}
