package com.opencredo.concourse.spring.demo.repositories;

import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.mapping.events.methods.state.StateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class GroupStateRepository {

    private final EventSource eventSource;

    @Autowired
    public GroupStateRepository(EventSource eventSource) {
        this.eventSource = eventSource;
    }

    public Optional<GroupState> getGroupState(UUID groupId) {
        return StateBuilder.forStateClass(GroupState.class).buildState(eventSource, groupId)
                .filter(state -> !state.isDeleted());
    }
}
