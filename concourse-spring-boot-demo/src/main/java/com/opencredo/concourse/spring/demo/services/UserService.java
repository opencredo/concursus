package com.opencredo.concourse.spring.demo.services;

import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.events.sourcing.CachedEventSource;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.events.views.EventView;
import com.opencredo.concourse.mapping.annotations.HandlesEvent;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.events.methods.reflection.EventInterfaceReflection;
import com.opencredo.concourse.mapping.events.methods.state.StateBuilder;
import com.opencredo.concourse.spring.demo.events.*;
import com.opencredo.concourse.spring.demo.repositories.UserStateRepository;
import com.opencredo.concourse.spring.demo.views.UserView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class UserService {

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


    private Map<UUID, String> getGroupNames(Collection<UUID> userIds) {
        StateBuilder<GroupNameState> stateBuilder = StateBuilder.forStateClass(GroupNameState.class);

        CachedEventSource cachedEventSource = stateBuilder.preload(eventSource, userIds);

        return userIds.stream()
                .map(id -> stateBuilder.buildState(cachedEventSource, id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(
                        state -> state.id,
                        state -> state.name
                ));
    }

    @HandlesEventsFor("group")
    public static final class GroupNameState {

        @HandlesEvent
        public static GroupNameState created(UUID id, String groupName) {
            return new GroupNameState(id, groupName);
        }

        private final UUID id;
        private String name;

        private GroupNameState(UUID id, String name) {
            this.id = id;
            this.name = name;
        }

        @HandlesEvent
        public void changedName(String newName) {
            name = newName;
        }
    }

}
