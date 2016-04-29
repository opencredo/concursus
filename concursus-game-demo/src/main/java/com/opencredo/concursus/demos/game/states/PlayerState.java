package com.opencredo.concursus.demos.game.states;

import com.opencredo.concursus.mapping.annotations.HandlesEvent;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;

@HandlesEventsFor("player")
public final class PlayerState {

    @HandlesEvent
    public static PlayerState created(String playerId, String playerName) {
        return new PlayerState(playerId, playerName);
    }

    private int rating = 0;
    private final String id;
    private String name;

    public PlayerState(String id, String name) {
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
    public void wonGame(String gameId, int ratingIncrease) {
        rating += ratingIncrease;
    }

    @HandlesEvent
    public void lostGame(String gameId, int ratingDecrease) {
        rating -= ratingDecrease;
    }
}
