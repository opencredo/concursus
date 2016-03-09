package com.opencredo.concourse.demos.game.engine;

import org.springframework.stereotype.Component;

import java.util.function.IntBinaryOperator;

@Component
public final class ScoringAlgorithm implements IntBinaryOperator {

    @Override
    public int applyAsInt(int winnerScore, int loserScore) {
        int scoreDifference = loserScore - winnerScore;
        return 50 + Math.min(45, Math.max(-45, scoreDifference / 100));
    }

}
