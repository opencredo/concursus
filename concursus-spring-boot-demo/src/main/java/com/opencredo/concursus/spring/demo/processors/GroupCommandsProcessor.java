package com.opencredo.concursus.spring.demo.processors;

import com.opencredo.concursus.domain.events.state.StateRepository;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concursus.spring.commands.processing.CommandHandler;
import com.opencredo.concursus.spring.demo.commands.GroupCommands;
import com.opencredo.concursus.spring.demo.controllers.GroupNotFoundException;
import com.opencredo.concursus.spring.demo.events.GroupEvents;
import com.opencredo.concursus.spring.demo.events.UserEvents;
import com.opencredo.concursus.spring.demo.repositories.GroupState;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandHandler
public class GroupCommandsProcessor implements GroupCommands {

    private final ProxyingEventBus proxyingEventBus;
    private final StateRepository<GroupState> groupStateRepository;

    @Autowired
    public GroupCommandsProcessor(ProxyingEventBus proxyingEventBus, StateRepository<GroupState> groupStateRepository) {
        this.proxyingEventBus = proxyingEventBus;
        this.groupStateRepository = groupStateRepository;
    }

    @Override
    public CompletableFuture<UUID> create(StreamTimestamp ts, UUID groupId, String groupName) {
        proxyingEventBus.dispatch(GroupEvents.class, groupEvents -> groupEvents.created(ts, groupId, groupName));

        return CompletableFuture.completedFuture(groupId);
    }

    @Override
    public CompletableFuture<Void> delete(StreamTimestamp ts, UUID groupId) {
        GroupState groupState = groupStateRepository.getState(groupId).orElseThrow(GroupNotFoundException::new);

        proxyingEventBus.dispatch(UserEvents.class, GroupEvents.class, (userEvents, groupEvents) -> {
            groupState.getUsers().forEach(userId -> userEvents.removedFromGroup(ts, userId, groupId));
            groupEvents.deleted(ts, groupId);
        });

        return CompletableFuture.completedFuture(null);
    }
}
