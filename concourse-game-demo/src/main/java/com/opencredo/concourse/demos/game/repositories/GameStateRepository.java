package com.opencredo.concourse.demos.game.repositories;

import com.opencredo.concourse.demos.game.states.GameState;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.mapping.events.methods.state.StateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class GameStateRepository {

    private final EventSource eventSource;

    @Autowired
    public GameStateRepository(EventSource eventSource) {
        this.eventSource = eventSource;
    }

    public Optional<GameState> get(UUID gameId) {
        final StateBuilder<GameState> stateBuilder = StateBuilder.forStateClass(GameState.class);

        return stateBuilder.buildState(eventSource, gameId);
    }
}
