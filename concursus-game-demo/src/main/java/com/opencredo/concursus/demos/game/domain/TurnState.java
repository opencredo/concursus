package com.opencredo.concursus.demos.game.domain;

import java.util.UUID;

public final class TurnState {

    public static TurnState of(UUID playerOneId, UUID playerTwoId, PlayerIndex currentPlayer) {
        return new TurnState(playerOneId, playerTwoId, currentPlayer);
    }

    private final UUID playerOneId;
    private final UUID playerTwoId;
    private PlayerIndex currentPlayer;

    private TurnState(UUID playerOneId, UUID playerTwoId, PlayerIndex currentPlayer) {
        this.playerOneId = playerOneId;
        this.playerTwoId = playerTwoId;
        this.currentPlayer = currentPlayer;
    }

    public boolean isCurrentPlayer(UUID playerId) {
        return getCurrentPlayerId().equals(playerId);
    }

    public UUID getCurrentPlayerId() {
        return currentPlayer.equals(PlayerIndex.PLAYER_1)
                ? playerOneId
                : playerTwoId;
    }

    public void switchPlayers() {
        currentPlayer = currentPlayer.equals(PlayerIndex.PLAYER_1)
                ? PlayerIndex.PLAYER_2
                : PlayerIndex.PLAYER_1;
    }

    public UUID getPlayerOneId() {
        return playerOneId;
    }

    public UUID getPlayerTwoId() {
        return playerTwoId;
    }

    public PlayerIndex getCurrentPlayerIndex() {
        return currentPlayer;
    }

    public UUID getOpponentId() {
        return currentPlayer.equals(PlayerIndex.PLAYER_1)
                ? playerTwoId
                : playerOneId;
    }
}
