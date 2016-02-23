package com.opencredo.concourse.spring.demo.services;

import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.events.views.EventView;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.events.methods.dispatching.DispatchingCachedEventSource;
import com.opencredo.concourse.mapping.events.methods.dispatching.DispatchingEventSource;
import com.opencredo.concourse.mapping.events.methods.reflection.EventInterfaceReflection;
import com.opencredo.concourse.spring.demo.events.*;
import com.opencredo.concourse.spring.demo.repositories.UserStateRepository;
import com.opencredo.concourse.spring.demo.views.UserView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class UserService {

    @HandlesEventsFor("group")
    public interface GroupNameEvents extends GroupCreatedEvent, GroupChangedNameEvent {}

    private final EventSource eventSource;
    private final UserStateRepository userStateRepository;

    @Autowired
    public UserService(EventSource eventSource, UserStateRepository userStateRepository) {
        this.eventSource = eventSource;
        this.userStateRepository = userStateRepository;
    }

    public List<EventView> getHistory(UUID userId) {
        return eventSource.getEvents(EventInterfaceReflection.getEventTypeMatcher(UserEvents.class), AggregateId.of("user", userId))
                .stream().map(EventView::of).collect(Collectors.toList());
    }

    public boolean checkPassword(UUID userId, String passwordHash) {
        return userStateRepository.getUserState(userId).map(userState -> userState.getPasswordHash().equals(passwordHash)).orElse(false);
    }

    public Optional<UserView> getUser(UUID userId) {
        return userStateRepository.getUserState(userId).map(user ->
                new UserView(
                        user.getId(),
                        user.getName(),
                        getGroupNames(user.getGroupIds())));
    }

    private Map<UUID, String> getGroupNames(Collection<UUID> groupIds) {
        GroupNameHandler handler = new GroupNameHandler();

        final DispatchingCachedEventSource<GroupNameEvents> preloaded = DispatchingEventSource
                .dispatching(eventSource, GroupNameEvents.class)
                .preload(groupIds);

        groupIds.stream().map(preloaded::replaying).forEach(replayer -> replayer.replayAll(handler));

        return handler.getGroupNames();
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
