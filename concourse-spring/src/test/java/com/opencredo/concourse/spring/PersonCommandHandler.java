package com.opencredo.concourse.spring;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concourse.spring.commands.processing.CommandHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandHandler
public class PersonCommandHandler implements PersonCommands {

    private final ProxyingEventBus eventBus;

    @Autowired
    public PersonCommandHandler(ProxyingEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public CompletableFuture<Void> create(StreamTimestamp ts, UUID personId, String name, int age) {
        eventBus.getDispatcherFor(PersonEvents.class).created(ts, personId, name, age);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateNameAndAge(StreamTimestamp ts, UUID personId, String newName, int newAge) {
        eventBus.dispatch(PersonEvents.class, events -> {
            events.updatedAge(ts.subStream("age"), personId, newAge);
            events.updatedName(ts.subStream("name"), personId, newName);
        });
        return CompletableFuture.completedFuture(null);
    }
}
