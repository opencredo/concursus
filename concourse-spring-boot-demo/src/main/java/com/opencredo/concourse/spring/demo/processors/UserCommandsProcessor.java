package com.opencredo.concourse.spring.demo.processors;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concourse.spring.commands.processing.CommandHandler;
import com.opencredo.concourse.spring.demo.commands.UserCommands;
import com.opencredo.concourse.spring.demo.controllers.UserNotFoundException;
import com.opencredo.concourse.spring.demo.events.GroupEvents;
import com.opencredo.concourse.spring.demo.events.UserEvents;
import com.opencredo.concourse.spring.demo.services.GroupService;
import com.opencredo.concourse.spring.demo.services.UserService;
import com.opencredo.concourse.spring.demo.views.UserView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandHandler
public class UserCommandsProcessor implements UserCommands {

    private final UserService userService;
    private final GroupService groupService;
    private final ProxyingEventBus proxyingEventBus;

    @Autowired
    public UserCommandsProcessor(UserService userService, GroupService groupService, ProxyingEventBus proxyingEventBus) {
        this.userService = userService;
        this.groupService = groupService;
        this.proxyingEventBus = proxyingEventBus;
    }

    @Override
    public CompletableFuture<UUID> create(StreamTimestamp ts, UUID userId, String userName, byte[] passwordHash) {
        proxyingEventBus.dispatch(UserEvents.class, userEvents -> userEvents.created(ts, userId, userName, new String(passwordHash)));
        return CompletableFuture.completedFuture(userId);
    }

    @Override
    public CompletableFuture<Void> updateName(StreamTimestamp ts, UUID userId, String newName) {
        UserView userView = userService.getUser(userId).orElseThrow(UserNotFoundException::new);

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
        proxyingEventBus.dispatch(UserEvents.class, userEvents -> userEvents.deleted(ts, userId));
        return CompletableFuture.completedFuture(null);
    }
}
