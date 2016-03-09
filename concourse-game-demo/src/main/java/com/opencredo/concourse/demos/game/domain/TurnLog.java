package com.opencredo.concourse.demos.game.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class TurnLog {

    @JsonCreator
    public static TurnLog withOutcome(Outcome outcome) {
        return new TurnLog(outcome);
    }

    private final Outcome outcome;

    private TurnLog(Outcome outcome) {
        this.outcome = outcome;
    }

    @JsonProperty
    public Outcome getOutcome() {
        return outcome;
    }

}
