package com.opencredo.concourse.spring.demo.services;

import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.events.methods.collecting.ProxyingEventStreamCollector;
import com.opencredo.concourse.mapping.events.methods.dispatching.DispatchingCachedEventSource;
import com.opencredo.concourse.mapping.events.methods.dispatching.DispatchingEventSource;
import com.opencredo.concourse.spring.demo.events.GroupChangedNameEvent;
import com.opencredo.concourse.spring.demo.events.GroupCreatedEvent;
import com.opencredo.concourse.spring.demo.events.UserCreatedEvent;
import com.opencredo.concourse.spring.demo.events.UserUpdatedEvents;
import com.opencredo.concourse.spring.demo.views.UserView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UserService {

    public interface GroupNameEvents extends GroupCreatedEvent, GroupChangedNameEvent {}

    private final EventSource eventSource;

    @Autowired
    public UserService(EventSource eventSource) {
        this.eventSource = eventSource;
    }

    public boolean checkPassword(UUID userId, String passwordHash) {
        return getUserState(userId).map(userState -> userState.getPasswordHash().equals(passwordHash)).orElse(false);
    }

    public Optional<UserView> getUser(UUID userId) {
        return getUserState(userId).map(user ->
                new UserView(
                        user.getId(),
                        user.getName(),
                        getGroupNames(user.getGroupIds())));
    }

    private Optional<UserState> getUserState(UUID userId) {
        ProxyingEventStreamCollector<UserState, UserCreatedEvent, UserUpdatedEvents> collector = ProxyingEventStreamCollector.proxying(
                UserState.class,
                UserCreatedEvent.class,
                UserUpdatedEvents.class
        );

        return collector.collect(eventSource, userId,
                caller -> (ts, id, name, pwd) -> caller.accept(new UserState(id, name, pwd)),
                UserUpdater::new)
                .filter(userState -> !userState.isDeleted());
    }

    private Map<UUID, String> getGroupNames(Collection<UUID> groupIds) {
        GroupNameHandler handler = new GroupNameHandler();

        final DispatchingCachedEventSource<GroupNameEvents> preloaded = DispatchingEventSource
                .dispatching(eventSource, GroupNameEvents.class)
                .preload(groupIds);

        groupIds.stream().map(preloaded::replaying).forEach(replayer -> replayer.replayAll(handler));

        return handler.getGroupNames();
    }

    private static class UserUpdater implements UserUpdatedEvents {
        private final UserState userState;

        public UserUpdater(UserState userState) {
            this.userState = userState;
        }

        @Override
        public void changedName(StreamTimestamp ts, UUID userId, String newName) {
            userState.setName(newName);
        }

        @Override
        public void addedToGroup(StreamTimestamp ts, UUID userId, UUID groupId) {
            userState.addGroupId(groupId);
        }

        @Override
        public void removedFromGroup(StreamTimestamp ts, UUID userId, UUID groupId) {
            userState.removeGroupId(groupId);
        }

        @Override
        public void deleted(StreamTimestamp ts, UUID userId) {
            userState.delete();
        }

        @Override
        public void updatedPassword(StreamTimestamp ts, UUID userId, String newPasswordHash) {
            userState.setPasswordHash(newPasswordHash);
        }
    }

    private static class GroupNameHandler implements GroupNameEvents {
        private final Map<UUID, String> groupNames = new HashMap<>();

        @Override
        public void created(StreamTimestamp ts, UUID groupId, String groupName) {
            groupNames.put(groupId, groupName);
        }

        @Override
        public void changedName(StreamTimestamp ts, UUID groupId, String newName) {
            groupNames.put(groupId, newName);
        }

        public Map<UUID, String> getGroupNames() {
            return groupNames;
        }
    }
}
