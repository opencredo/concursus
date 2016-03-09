package com.opencredo.concourse.demos.game.states;

import com.opencredo.concourse.mapping.annotations.HandlesEvent;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;

import java.util.UUID;

@HandlesEventsFor("player")
public final class PlayerState {

    @HandlesEvent
    public static PlayerState created(UUID playerId, String playerName) {
        return new PlayerState(playerId, playerName);
    }

    private int rating = 0;
    private final UUID id;
    private String name;

    public PlayerState(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getRating() {
        return rating;
    }

    @HandlesEvent
    public void changedName(String newPlayerName) {
        this.name = newPlayerName;
    }

    @HandlesEvent
    public void wonGame(UUID gameId, int ratingIncrease) {
        rating += ratingIncrease;
    }

    @HandlesEvent
    public void lostGame(UUID gameId, int ratingDecrease) {
        rating -= ratingDecrease;
    }
}
