package com.opencredo.concursus.demos.game.engine;

import com.opencredo.concursus.demos.game.states.PlayerState;
import com.opencredo.concursus.domain.events.state.StateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public final class PointsCalculator {

    private final StateRepository<PlayerState> playerStateRepository;
    private final ScoringAlgorithm scoringAlgorithm;

    @Autowired
    public PointsCalculator(StateRepository<PlayerState> playerStateRepository, ScoringAlgorithm scoringAlgorithm) {
        this.playerStateRepository = playerStateRepository;
        this.scoringAlgorithm = scoringAlgorithm;
    }

    public int calculatePoints(String winnerId, String loserId) {
        Map<String, PlayerState> playerStates = playerStateRepository.getStates(winnerId, loserId);
        int winnerScore = playerStates.get(winnerId).getRating();
        int loserScore = playerStates.get(loserId).getRating();

        return scoringAlgorithm.applyAsInt(winnerScore, loserScore);
    }
}
