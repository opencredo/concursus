package com.opencredo.concourse.demos.game.engine;

import com.opencredo.concourse.demos.game.repositories.PlayerStateRepository;
import com.opencredo.concourse.demos.game.states.PlayerState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Component
public final class PointsCalculator {

    private final PlayerStateRepository playerStateRepository;
    private final ScoringAlgorithm scoringAlgorithm;

    @Autowired
    public PointsCalculator(PlayerStateRepository playerStateRepository, ScoringAlgorithm scoringAlgorithm) {
        this.playerStateRepository = playerStateRepository;
        this.scoringAlgorithm = scoringAlgorithm;
    }

    public int calculatePoints(UUID winnerId, UUID loserId) {
        Map<UUID, PlayerState> playerStates = playerStateRepository.getAll(Arrays.asList(winnerId, loserId));
        int winnerScore = playerStates.get(winnerId).getRating();
        int loserScore = playerStates.get(loserId).getRating();

        return scoringAlgorithm.applyAsInt(winnerScore, loserScore);
    }
}
