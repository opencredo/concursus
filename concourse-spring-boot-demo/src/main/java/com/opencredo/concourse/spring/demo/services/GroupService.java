package com.opencredo.concourse.spring.demo.services;

import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.events.methods.collecting.ProxyingEventStreamCollector;
import com.opencredo.concourse.mapping.events.methods.dispatching.DispatchingCachedEventSource;
import com.opencredo.concourse.mapping.events.methods.dispatching.DispatchingEventSource;
import com.opencredo.concourse.spring.demo.events.GroupCreatedEvent;
import com.opencredo.concourse.spring.demo.events.GroupUpdatedEvents;
import com.opencredo.concourse.spring.demo.events.UserChangedNameEvent;
import com.opencredo.concourse.spring.demo.events.UserCreatedEvent;
import com.opencredo.concourse.spring.demo.views.GroupView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GroupService {

    @HandlesEventsFor("user")
    public interface UserNameEvents extends UserCreatedEvent, UserChangedNameEvent {}

    private final EventSource eventSource;

    @Autowired
    public GroupService(EventSource eventSource) {
        this.eventSource = eventSource;
    }

    public Optional<GroupView> getGroup(UUID groupId) {
        return getGroupState(groupId).map(group -> new GroupView(group.getId(), group.getName(), getUserNames(group.getUsers())));
    }

    private Optional<GroupState> getGroupState(UUID groupId) {
        ProxyingEventStreamCollector<GroupState, GroupCreatedEvent, GroupUpdatedEvents> collector = ProxyingEventStreamCollector.proxying(
                GroupState.class,
                GroupCreatedEvent.class,
                GroupUpdatedEvents.class);

        return collector.collect(eventSource, groupId,
                caller -> (ts, id, name) -> caller.accept(new GroupState(id, name)),
                GroupStateUpdater::new)
                .filter(g -> !g.isDeleted());
    }

    private Map<UUID, String> getUserNames(Collection<UUID> userIds) {
        UserNameHandler handler = new UserNameHandler();

        final DispatchingCachedEventSource<UserNameEvents> preloaded = DispatchingEventSource
                .dispatching(eventSource, UserNameEvents.class)
                .preload(userIds);

        userIds.stream().map(preloaded::replaying).forEach(replayer -> replayer.replayAll(handler));

        return handler.getUserNames();
    }

    private static class GroupStateUpdater implements GroupUpdatedEvents {
        private final GroupState groupState;

        public GroupStateUpdater(GroupState groupState) {
            this.groupState = groupState;
        }

        @Override
        public void userAdded(StreamTimestamp ts, UUID groupId, UUID userId) {
            groupState.addUser(userId);
        }

        @Override
        public void userRemoved(StreamTimestamp ts, UUID groupId, UUID userId) {
            groupState.removeUser(userId);
        }

        @Override
        public void deleted(StreamTimestamp ts, UUID groupId) {
            groupState.delete();
        }

        @Override
        public void changedName(StreamTimestamp ts, UUID groupId, String newName) {
            groupState.setName(newName);
        }
    }

    private static class UserNameHandler implements UserNameEvents {
        private final Map<UUID, String> userNames = new HashMap<>();

        @Override
        public void created(StreamTimestamp ts, UUID userId, String userName, String passwordHash) {
            userNames.put(userId, userName);
        }

        @Override
        public void changedName(StreamTimestamp ts, UUID userId, String newName) {
            userNames.put(userId, newName);
        }

        public Map<UUID, String> getUserNames() {
            return userNames;
        }
    }
}
