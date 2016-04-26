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
    public String create(StreamTimestamp ts, String groupId, String groupName) {
        proxyingEventBus.dispatch(GroupEvents.class, groupEvents -> groupEvents.created(ts, groupId, groupName));

        return groupId;
    }

    @Override
    public void delete(StreamTimestamp ts, String groupId) {
        GroupState groupState = groupStateRepository.getState(groupId).orElseThrow(GroupNotFoundException::new);

        proxyingEventBus.dispatch(UserEvents.class, GroupEvents.class, (userEvents, groupEvents) -> {
            groupState.getUsers().forEach(userId -> userEvents.removedFromGroup(ts, userId, groupId));
            groupEvents.deleted(ts, groupId);
        });
    }
}
