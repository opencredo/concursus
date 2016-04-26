package com.opencredo.concursus.demos.game.states;

import com.opencredo.concursus.demos.game.domain.*;
import com.opencredo.concursus.demos.game.exceptions.IllegalGameStateException;
import com.opencredo.concursus.mapping.annotations.HandlesEvent;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;
import com.opencredo.concursus.mapping.annotations.Ordered;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@HandlesEventsFor("game")
public class GameState {

    @HandlesEvent
    public static GameState gameCreated(String gameId, String playerOneId, String rulesetVersion) {
        return new GameState(gameId, rulesetVersion, playerOneId);
    }

    private final String gameId;
    private final String rulesetVersion;
    private final String initiatingPlayerId;
    private Optional<String> joiningPlayerId;
    private Optional<TurnState> turnState = Optional.empty();
    private BoardState boardState = BoardState.empty();

    private List<Card> playerOneCards = Collections.emptyList();
    private List<Card> playerTwoCards = Collections.emptyList();

    public GameState(String gameId, String rulesetVersion, String initiatingPlayerId) {
        this.gameId = gameId;
        this.rulesetVersion = rulesetVersion;
        this.initiatingPlayerId = initiatingPlayerId;
    }

    public boolean isAwaitingSecondPlayer() {
        return !turnState.isPresent();
    }

    public String getRulesetVersion() {
        return rulesetVersion;
    }

    public boolean isPlayersTurn(String playerId) {
        return turnState.map(turnState -> turnState.isCurrentPlayer(playerId)).orElse(false);
    }

    public String getPlayerOneId() {
        return turnState.map(TurnState::getPlayerOneId).orElseThrow(() -> new IllegalGameStateException("Game not started"));
    }

    public String getPlayerTwoId() {
        return turnState.map(TurnState::getPlayerTwoId).orElseThrow(() -> new IllegalGameStateException("Game not started"));
    }

    @Ordered(0)
    @HandlesEvent
    public void playerTwoJoined(String playerTwoId) {
        joiningPlayerId = Optional.of(playerTwoId);
    }

    @Ordered(1)
    @HandlesEvent
    public void gameStarted(List<Card> playerOneDeck, List<Card> playerTwoDeck, PlayerIndex firstPlayer) {
        turnState = Optional.of(TurnState.of(
                initiatingPlayerId,
                joiningPlayerId.orElseThrow(() -> new IllegalGameStateException("Second player not joined")),
                firstPlayer));
        playerOneCards = playerOneDeck;
        playerTwoCards = playerTwoDeck;
    }

    @Ordered(2)
    @HandlesEvent
    public void playerOneTurn(Card card, Optional<BoardSlot> toSlot, TurnLog turnLog) {
        boardState.accept(PlayerIndex.PLAYER_1, turnLog);
        turnState.ifPresent(TurnState::switchPlayers);
    }

    @Ordered(2)
    @HandlesEvent
    public void playerTwoTurn(Card card, Optional<BoardSlot> toSlot, TurnLog turnLog) {
        boardState.accept(PlayerIndex.PLAYER_2, turnLog);
        turnState.ifPresent(TurnState::switchPlayers);
    }

    public PlayerIndex getCurrentPlayerIndex() {
        return turnState.map(TurnState::getCurrentPlayerIndex).orElseThrow(() -> new IllegalGameStateException("Game not started"));
    }

    public BoardState getBoardState() {
        return boardState;
    }

    public TurnState getTurnState() {
        return turnState.orElseThrow(() -> new IllegalGameStateException("Game not started"));
    }
}
