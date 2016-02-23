package com.opencredo.concourse.spring.demo.processors;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concourse.spring.commands.processing.CommandHandler;
import com.opencredo.concourse.spring.demo.commands.UserCommands;
import com.opencredo.concourse.spring.demo.controllers.UserNotFoundException;
import com.opencredo.concourse.spring.demo.events.GroupEvents;
import com.opencredo.concourse.spring.demo.events.UserEvents;
import com.opencredo.concourse.spring.demo.repositories.UserState;
import com.opencredo.concourse.spring.demo.repositories.UserStateRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandHandler
public class UserCommandsProcessor implements UserCommands {

    private final UserStateRepository userStateRepository;
    private final ProxyingEventBus proxyingEventBus;

    @Autowired
    public UserCommandsProcessor(UserStateRepository userStateRepository, ProxyingEventBus proxyingEventBus) {
        this.userStateRepository = userStateRepository;
        this.proxyingEventBus = proxyingEventBus;
    }

    @Override
    public CompletableFuture<UUID> create(StreamTimestamp ts, UUID userId, String userName, byte[] passwordHash) {
        proxyingEventBus.dispatch(UserEvents.class, userEvents -> userEvents.created(ts, userId, userName, new String(passwordHash)));
        return CompletableFuture.completedFuture(userId);
    }

    @Override
    public CompletableFuture<Void> updateName(StreamTimestamp ts, UUID userId, String newName) {
        proxyingEventBus.dispatch(UserEvents.class, userEvents -> userEvents.changedName(ts, userId, newName));

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> addToGroup(StreamTimestamp ts, UUID userId, UUID groupId) {
        proxyingEventBus.dispatch(UserEvents.class, GroupEvents.class, (userEvents, groupEvents) -> {
            userEvents.addedToGroup(ts, userId, groupId);
            groupEvents.userAdded(ts, groupId, userId);
        });

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> removeFromGroup(StreamTimestamp ts, UUID userId, UUID groupId) {
        proxyingEventBus.dispatch(UserEvents.class, GroupEvents.class, (userEvents, groupEvents) -> {
            userEvents.removedFromGroup(ts, userId, groupId);
            groupEvents.userRemoved(ts, groupId, userId);
        });

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> delete(StreamTimestamp ts, UUID userId) {
        UserState userState = userStateRepository.getUserState(userId).orElseThrow(UserNotFoundException::new);

        proxyingEventBus.dispatch(UserEvents.class, GroupEvents.class, (userEvents, groupEvents) -> {
            userState.getGroupIds().forEach(groupId -> groupEvents.userRemoved(ts, groupId, userId));
            userEvents.deleted(ts, userId);
        });

        return CompletableFuture.completedFuture(null);
    }
}
