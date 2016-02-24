package com.opencredo.concourse.spring.demo.services;

import com.opencredo.concourse.domain.events.sourcing.CachedEventSource;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.mapping.annotations.HandlesEvent;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;
import com.opencredo.concourse.mapping.events.methods.state.StateBuilder;
import com.opencredo.concourse.spring.demo.repositories.GroupStateRepository;
import com.opencredo.concourse.spring.demo.views.GroupView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GroupService {

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
        StateBuilder<UserNameState> stateBuilder = StateBuilder.forStateClass(UserNameState.class);

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

    @HandlesEventsFor("user")
    public static final class UserNameState {

        @HandlesEvent
        public static UserNameState created(UUID id, String userName, String passwordHash) {
            return new UserNameState(id, userName);
        }

        private final UUID id;
        private String name;

        private UserNameState(UUID id, String name) {
            this.id = id;
            this.name = name;
        }

        @HandlesEvent
        public void changedName(String newName) {
            name = newName;
        }
    }

}
