package com.opencredo.concursus.demos.game.commands;

import com.opencredo.concursus.demos.game.domain.BoardSlot;
import com.opencredo.concursus.demos.game.domain.Card;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesCommandsFor;

import java.util.Optional;
import java.util.UUID;

@HandlesCommandsFor("player")
public interface PlayerCommands {

    UUID create(StreamTimestamp ts, UUID playerId, String playerName);
    void delete(StreamTimestamp ts, UUID playerId);
    void changeName(StreamTimestamp ts, UUID playerId, String newPlayerName);

    UUID startGame(StreamTimestamp ts, UUID playerId, String rulesetVersion);
    void joinGame(StreamTimestamp ts, UUID playerId, UUID gameId);

    void playTurn(StreamTimestamp ts, UUID playerId, UUID gameId, Card card, Optional<BoardSlot> toSlot);

    void surrender(StreamTimestamp ts, UUID playerId, UUID gameId);
}
