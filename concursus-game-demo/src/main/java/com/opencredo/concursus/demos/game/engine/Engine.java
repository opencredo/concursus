package com.opencredo.concursus.demos.game.engine;

import com.opencredo.concursus.demos.game.domain.*;

import java.util.Optional;

@FunctionalInterface
public interface Engine {

    default Deal deal() {
        return Deal.from(Card.values());
    }

    TurnLog applyTurn(PlayerIndex playerIndex, BoardState boardState, Card card, Optional<BoardSlot> targetSlot);
}
