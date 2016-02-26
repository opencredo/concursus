package com.opencredo.concourse.spring.demo.repositories;

import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.mapping.events.methods.state.StateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserStateRepository {

    private final EventSource eventSource;

    @Autowired
    public UserStateRepository(EventSource eventSource) {
        this.eventSource = eventSource;
    }

    public Optional<UserState> getUserState(UUID userId) {
        return StateBuilder.forStateClass(UserState.class).buildState(eventSource, userId)
                .filter(state -> !state.isDeleted());
    }

    public Map<UUID, UserState> getUserStates(Collection<UUID> userIds) {
        return StateBuilder.forStateClass(UserState.class).buildStates(eventSource, userIds);
    }

}
