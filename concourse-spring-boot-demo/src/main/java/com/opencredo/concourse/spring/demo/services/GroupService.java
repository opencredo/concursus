package com.opencredo.concourse.spring.demo.services;

import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.events.methods.dispatching.DispatchingCachedEventSource;
import com.opencredo.concourse.mapping.events.methods.dispatching.DispatchingEventSource;
import com.opencredo.concourse.spring.demo.events.UserChangedNameEvent;
import com.opencredo.concourse.spring.demo.events.UserCreatedEvent;
import com.opencredo.concourse.spring.demo.repositories.GroupStateRepository;
import com.opencredo.concourse.spring.demo.views.GroupView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GroupService {

    @HandlesEventsFor("user")
    public interface UserNameEvents extends UserCreatedEvent, UserChangedNameEvent {}

    private final EventSource eventSource;
    private final GroupStateRepository groupStateRepository;

    @Autowired
    public GroupService(EventSource eventSource, GroupStateRepository groupStateRepository) {
        this.eventSource = eventSource;
        this.groupStateRepository = groupStateRepository;
    }

    public Optional<GroupView> getGroup(UUID groupId) {
        return groupStateRepository.getGroupState(groupId).map(group -> new GroupView(group.getId(), group.getName(), getUserNames(group.getUsers())));
    }

    private Map<UUID, String> getUserNames(Collection<UUID> userIds) {
        UserNameHandler handler = new UserNameHandler();

        final DispatchingCachedEventSource<UserNameEvents> preloaded = DispatchingEventSource
                .dispatching(eventSource, UserNameEvents.class)
                .preload(userIds);

        userIds.stream().map(preloaded::replaying).forEach(replayer -> replayer.replayAll(handler));

        return handler.getUserNames();
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
