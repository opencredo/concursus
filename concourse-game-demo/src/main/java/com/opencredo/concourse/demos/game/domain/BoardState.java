package com.opencredo.concourse.demos.game.domain;

import java.util.function.BiConsumer;

public final class BoardState implements BiConsumer<PlayerIndex, TurnLog> {

    public static BoardState empty() {
        return new BoardState();
    }

    private BoardState() {
    }

    @Override
    public void accept(PlayerIndex playerIndex, TurnLog turnLog) {
        // turn log modifies board state
    }
}
