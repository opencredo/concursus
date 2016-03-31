package com.opencredo.concursus.demos.game.events;

import com.opencredo.concursus.demos.game.domain.BoardSlot;
import com.opencredo.concursus.demos.game.domain.Card;
import com.opencredo.concursus.demos.game.domain.PlayerIndex;
import com.opencredo.concursus.demos.game.domain.TurnLog;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;
import com.opencredo.concursus.mapping.annotations.Initial;
import com.opencredo.concursus.mapping.annotations.Ordered;
import com.opencredo.concursus.mapping.annotations.Terminal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@HandlesEventsFor("game")
public interface GameEvents {

    @Initial
    void gameCreated(StreamTimestamp ts, UUID gameId, UUID playerOneId, String rulesetVersion);

    @Ordered(0)
    void playerTwoJoined(StreamTimestamp ts, UUID gameId, UUID playerTwoId);

    @Ordered(1)
    void gameStarted(StreamTimestamp ts, UUID gameId, List<Card> playerOneDeck, List<Card> playerTwoDeck, PlayerIndex firstPlayer);

    @Ordered(2)
    void playerOneTurn(StreamTimestamp ts, UUID gameId, Card card, Optional<BoardSlot> toSlot, TurnLog turnLog);

    @Ordered(2)
    void playerTwoTurn(StreamTimestamp ts, UUID gameId, Card card, Optional<BoardSlot> toSlot, TurnLog turnLog);

    @Terminal
    void playerOneVictory(StreamTimestamp ts, UUID gameId);

    @Terminal
    void playerTwoVictory(StreamTimestamp ts, UUID gameId);

    @Terminal
    void playerOneSurrender(StreamTimestamp ts, UUID gameId);

    @Terminal
    void playerTwoSurrender(StreamTimestamp ts, UUID gameId);

}
