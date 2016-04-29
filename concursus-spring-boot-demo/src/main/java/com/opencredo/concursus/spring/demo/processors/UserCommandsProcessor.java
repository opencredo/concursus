package com.opencredo.concursus.spring.demo.processors;

import com.opencredo.concursus.domain.events.state.StateRepository;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concursus.spring.commands.processing.CommandHandler;
import com.opencredo.concursus.spring.demo.commands.UserCommands;
import com.opencredo.concursus.spring.demo.controllers.UserNotFoundException;
import com.opencredo.concursus.spring.demo.events.GroupEvents;
import com.opencredo.concursus.spring.demo.events.UserEvents;
import com.opencredo.concursus.spring.demo.repositories.UserState;
import org.springframework.beans.factory.annotation.Autowired;

@CommandHandler
public class UserCommandsProcessor implements UserCommands {

    private final StateRepository<UserState> userStateRepository;
    private final ProxyingEventBus proxyingEventBus;

    @Autowired
    public UserCommandsProcessor(StateRepository<UserState> userStateRepository, ProxyingEventBus proxyingEventBus) {
        this.userStateRepository = userStateRepository;
        this.proxyingEventBus = proxyingEventBus;
    }

    @Override
    public String create(StreamTimestamp ts, String userId, String userName, byte[] passwordHash) {
        proxyingEventBus.dispatch(UserEvents.class, userEvents -> userEvents.created(ts, userId, userName, new String(passwordHash)));
        return userId;
    }

    @Override
    public void updateName(StreamTimestamp ts, String userId, String newName) {
        proxyingEventBus.dispatch(UserEvents.class, userEvents -> userEvents.changedName(ts, userId, newName));
    }

    @Override
    public void addToGroup(StreamTimestamp ts, String userId, String groupId) {
        proxyingEventBus.dispatch(UserEvents.class, GroupEvents.class, (userEvents, groupEvents) -> {
            userEvents.addedToGroup(ts, userId, groupId);
            groupEvents.userAdded(ts, groupId, userId);
        });
    }

    @Override
    public void removeFromGroup(StreamTimestamp ts, String userId, String groupId) {
        proxyingEventBus.dispatch(UserEvents.class, GroupEvents.class, (userEvents, groupEvents) -> {
            userEvents.removedFromGroup(ts, userId, groupId);
            groupEvents.userRemoved(ts, groupId, userId);
        });
    }

    @Override
    public void delete(StreamTimestamp ts, String userId) {
        UserState userState = userStateRepository.getState(userId).orElseThrow(UserNotFoundException::new);

        proxyingEventBus.dispatch(UserEvents.class, GroupEvents.class, (userEvents, groupEvents) -> {
            userState.getGroupIds().forEach(groupId -> groupEvents.userRemoved(ts, groupId, userId));
            userEvents.deleted(ts, userId);
        });
    }
}
