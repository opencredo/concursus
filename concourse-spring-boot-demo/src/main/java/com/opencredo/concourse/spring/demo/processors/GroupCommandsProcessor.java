package com.opencredo.concourse.spring.demo.processors;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concourse.spring.commands.processing.CommandHandler;
import com.opencredo.concourse.spring.demo.commands.GroupCommands;
import com.opencredo.concourse.spring.demo.controllers.GroupNotFoundException;
import com.opencredo.concourse.spring.demo.events.GroupEvents;
import com.opencredo.concourse.spring.demo.events.UserEvents;
import com.opencredo.concourse.spring.demo.repositories.GroupState;
import com.opencredo.concourse.spring.demo.repositories.GroupStateRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandHandler
public class GroupCommandsProcessor implements GroupCommands {

    private final ProxyingEventBus proxyingEventBus;
    private final GroupStateRepository groupStateRepository;

    @Autowired
    public GroupCommandsProcessor(ProxyingEventBus proxyingEventBus, GroupStateRepository groupStateRepository) {
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
        GroupState groupState = groupStateRepository.getGroupState(groupId).orElseThrow(GroupNotFoundException::new);

        proxyingEventBus.dispatch(UserEvents.class, GroupEvents.class, (userEvents, groupEvents) -> {
            groupState.getUsers().forEach(userId -> userEvents.removedFromGroup(ts, userId, groupId));
            groupEvents.deleted(ts, groupId);
        });

        return CompletableFuture.completedFuture(null);
    }
}
