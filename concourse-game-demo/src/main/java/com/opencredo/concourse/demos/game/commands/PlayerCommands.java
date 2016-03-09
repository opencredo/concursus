package com.opencredo.concourse.demos.game.commands;

import com.opencredo.concourse.demos.game.domain.BoardSlot;
import com.opencredo.concourse.demos.game.domain.Card;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesCommandsFor;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@HandlesCommandsFor("player")
public interface PlayerCommands {

    CompletableFuture<UUID> create(StreamTimestamp ts, UUID playerId, String playerName);
    CompletableFuture<UUID> delete(StreamTimestamp ts, UUID playerId);
    CompletableFuture<Void> changeName(StreamTimestamp ts, UUID playerId, String newPlayerName);

    CompletableFuture<UUID> startGame(StreamTimestamp ts, UUID playerId, String rulesetVersion);
    CompletableFuture<Void> joinGame(StreamTimestamp ts, UUID playerId, UUID gameId);

    CompletableFuture<Void> playTurn(StreamTimestamp ts, UUID playerId, UUID gameId, Card card, Optional<BoardSlot> toSlot);

    CompletableFuture<Void> surrender(StreamTimestamp ts, UUID playerId, UUID gameId);
}
