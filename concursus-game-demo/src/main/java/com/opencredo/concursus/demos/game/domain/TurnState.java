package com.opencredo.concursus.demos.game.domain;

public final class TurnState {

    public static TurnState of(String playerOneId, String playerTwoId, PlayerIndex currentPlayer) {
        return new TurnState(playerOneId, playerTwoId, currentPlayer);
    }

    private final String playerOneId;
    private final String playerTwoId;
    private PlayerIndex currentPlayer;

    private TurnState(String playerOneId, String playerTwoId, PlayerIndex currentPlayer) {
        this.playerOneId = playerOneId;
        this.playerTwoId = playerTwoId;
        this.currentPlayer = currentPlayer;
    }

    public boolean isCurrentPlayer(String playerId) {
        return getCurrentPlayerId().equals(playerId);
    }

    public String getCurrentPlayerId() {
        return currentPlayer.equals(PlayerIndex.PLAYER_1)
                ? playerOneId
                : playerTwoId;
    }

    public void switchPlayers() {
        currentPlayer = currentPlayer.equals(PlayerIndex.PLAYER_1)
                ? PlayerIndex.PLAYER_2
                : PlayerIndex.PLAYER_1;
    }

    public String getPlayerOneId() {
        return playerOneId;
    }

    public String getPlayerTwoId() {
        return playerTwoId;
    }

    public PlayerIndex getCurrentPlayerIndex() {
        return currentPlayer;
    }

    public String getOpponentId() {
        return currentPlayer.equals(PlayerIndex.PLAYER_1)
                ? playerTwoId
                : playerOneId;
    }
}
