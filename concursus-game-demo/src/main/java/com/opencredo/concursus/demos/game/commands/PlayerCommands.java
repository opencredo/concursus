package com.opencredo.concursus.demos.game.commands;

import com.opencredo.concursus.demos.game.domain.BoardSlot;
import com.opencredo.concursus.demos.game.domain.Card;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesCommandsFor;

import java.util.Optional;

@HandlesCommandsFor("player")
public interface PlayerCommands {

    String create(StreamTimestamp ts, String playerId, String playerName);
    void delete(StreamTimestamp ts, String playerId);
    void changeName(StreamTimestamp ts, String playerId, String newPlayerName);

    String startGame(StreamTimestamp ts, String playerId, String rulesetVersion);
    void joinGame(StreamTimestamp ts, String playerId, String gameId);

    void playTurn(StreamTimestamp ts, String playerId, String gameId, Card card, Optional<BoardSlot> toSlot);

    void surrender(StreamTimestamp ts, String playerId, String gameId);
}
