package com.opencredo.concourse.demos.game.repositories;

import com.opencredo.concourse.demos.game.states.PlayerState;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.mapping.events.methods.state.StateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class PlayerStateRepository {

    private final EventSource eventSource;

    @Autowired
    public PlayerStateRepository(EventSource eventSource) {
        this.eventSource = eventSource;
    }

    public Optional<PlayerState> get(UUID playerIds) {
        return StateBuilder.forStateClass(PlayerState.class).buildState(eventSource, playerIds);
    }

    public Map<UUID, PlayerState> getAll(Collection<UUID> playerIds) {
        return StateBuilder.forStateClass(PlayerState.class).buildStates(eventSource, playerIds);
    }
}
