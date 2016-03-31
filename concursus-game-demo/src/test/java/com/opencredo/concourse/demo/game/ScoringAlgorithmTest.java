package com.opencredo.concursus.demo.game;

import com.opencredo.concursus.demos.game.engine.ScoringAlgorithm;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ScoringAlgorithmTest {

    private final ScoringAlgorithm scoringAlgorithm = new ScoringAlgorithm();

    @Test
    public void winsAgainstEqualPlayersAreWorthFifty() {
        assertThat(scoringAlgorithm.applyAsInt(100, 100), equalTo(50));
    }

    @Test
    public void winsAgainstStrongerPlayersAreWorthMore() {
        assertThat(scoringAlgorithm.applyAsInt(100, 200), equalTo(51));
    }

    @Test
    public void winsAgainstWeakerPlayersAreWorthLess() {
        assertThat(scoringAlgorithm.applyAsInt(200, 100), equalTo(49));
    }

    @Test
    public void maximumScoreIsNinetyFive() {
        assertThat(scoringAlgorithm.applyAsInt(0, Integer.MAX_VALUE), equalTo(95));
    }

    @Test
    public void minimumScoreIsFive() {
        assertThat(scoringAlgorithm.applyAsInt(Integer.MAX_VALUE, 0), equalTo(5));
    }

}
