package com.opencredo.concursus.demos.game.processors;

import com.opencredo.concursus.demos.game.commands.PlayerCommands;
import com.opencredo.concursus.demos.game.domain.*;
import com.opencredo.concursus.demos.game.engine.Deal;
import com.opencredo.concursus.demos.game.engine.EngineRegistry;
import com.opencredo.concursus.demos.game.engine.PointsCalculator;
import com.opencredo.concursus.demos.game.events.GameEvents;
import com.opencredo.concursus.demos.game.events.PlayerEvents;
import com.opencredo.concursus.demos.game.exceptions.IllegalGameStateException;
import com.opencredo.concursus.demos.game.exceptions.NoSuchGameException;
import com.opencredo.concursus.demos.game.states.GameState;
import com.opencredo.concursus.domain.state.StateRepository;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.events.methods.proxying.ProxyingEventBus;
import com.opencredo.concursus.spring.commands.processing.CommandHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

@CommandHandler
public final class PlayerCommandProcessor implements PlayerCommands {

    private final ProxyingEventBus eventBus;
    private final EngineRegistry engineRegistry;
    private final StateRepository<GameState> gameStateRepository;
    private final PointsCalculator pointsCalculator;

    @Autowired
    public PlayerCommandProcessor(ProxyingEventBus eventBus, EngineRegistry engineRegistry, StateRepository<GameState> gameStateRepository, PointsCalculator pointsCalculator) {
        this.eventBus = eventBus;
        this.engineRegistry = engineRegistry;
        this.gameStateRepository = gameStateRepository;
        this.pointsCalculator = pointsCalculator;
    }

    @Override
    public CompletableFuture<UUID> create(StreamTimestamp ts, UUID playerId, String playerName) {
        eventBus.dispatch(PlayerEvents.class, player -> player.created(ts, playerId, playerName));
        return completedFuture(playerId);
    }

    @Override
    public CompletableFuture<UUID> delete(StreamTimestamp ts, UUID playerId) {
        eventBus.dispatch(PlayerEvents.class, player -> player.deleted(ts, playerId));
        return completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> changeName(StreamTimestamp ts, UUID playerId, String newPlayerName) {
        eventBus.dispatch(PlayerEvents.class, player -> player.changedName(ts, playerId, newPlayerName));
        return completedFuture(null);
    }

    @Override
    public CompletableFuture<UUID> startGame(StreamTimestamp ts, UUID playerId, String rulesetVersion) {
        UUID gameId = UUID.randomUUID();

        eventBus.dispatch(PlayerEvents.class, GameEvents.class, (player, game) -> {
            player.startedGame(ts, playerId, gameId);
            game.gameCreated(ts, gameId, playerId, rulesetVersion);
        });

        return completedFuture(gameId);
    }

    @Override
    public CompletableFuture<Void> joinGame(StreamTimestamp ts, UUID playerId, UUID gameId) {
        GameState gameState = gameStateRepository.getState(gameId).orElseThrow(NoSuchGameException::new);

        if (!gameState.isAwaitingSecondPlayer()) {
            throw new IllegalStateException("Second player already joined");
        }

        Deal deal = engineRegistry.forRulesetVersion(gameState.getRulesetVersion()).deal();

        eventBus.dispatch(PlayerEvents.class, GameEvents.class, (player, game) -> {
            player.joinedGame(ts, playerId, gameId);
            game.playerTwoJoined(ts, gameId, playerId);
            game.gameStarted(ts.subStream("dealer"), gameId, deal.getPlayerOneCards(), deal.getPlayerTwoCards(), deal.getFirstPlayerIndex());
        });

        return completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> playTurn(StreamTimestamp ts, UUID playerId, UUID gameId, Card card, Optional<BoardSlot> toSlot) {
        GameState gameState = gameStateRepository.getState(gameId).orElseThrow(NoSuchGameException::new);

        if (!gameState.isPlayersTurn(playerId)) {
            throw new IllegalGameStateException("Not this player's turn");
        }

        TurnLog turnLog = engineRegistry.forRulesetVersion(gameState.getRulesetVersion())
                .applyTurn(
                    gameState.getCurrentPlayerIndex(),
                    gameState.getBoardState(),
                    card, toSlot);
        Outcome outcome = turnLog.getOutcome();

        final PlayerIndex currentPlayerIndex = gameState.getCurrentPlayerIndex();

        eventBus.dispatch(PlayerEvents.class, GameEvents.class, (player, game) -> {
            if (currentPlayerIndex.equals(PlayerIndex.PLAYER_1)) {
                game.playerOneTurn(ts, gameId, card, toSlot, turnLog);
            } else {
                game.playerTwoTurn(ts, gameId, card, toSlot, turnLog);
            }

            if (outcome == Outcome.VICTORY) {
                recordVictory(ts, gameId, gameState.getTurnState(), player, game);
            }
        });

        return completedFuture(null);
    }

    private void recordVictory(StreamTimestamp ts, UUID gameId, TurnState turnState, PlayerEvents player, GameEvents game) {
        recordPoints(ts, gameId, turnState.getCurrentPlayerId(), turnState.getOpponentId(), player);

        if (turnState.getCurrentPlayerIndex() == PlayerIndex.PLAYER_1) {
            game.playerOneVictory(ts.subStream("engine"), gameId);
        } else {
            game.playerTwoVictory(ts.subStream("engine"), gameId);
        }
    }

    private void recordSurrender(StreamTimestamp ts, UUID gameId, TurnState turnState, PlayerEvents player, GameEvents game) {
        recordPoints(ts, gameId, turnState.getOpponentId(), turnState.getCurrentPlayerId(), player);

        if (turnState.getCurrentPlayerIndex() == PlayerIndex.PLAYER_1) {
            game.playerOneSurrender(ts.subStream("engine"), gameId);
        } else {
            game.playerTwoSurrender(ts.subStream("engine"), gameId);
        }
    }

    private void recordPoints(StreamTimestamp ts, UUID gameId, UUID winnerId, UUID loserId, PlayerEvents player) {
        int points = pointsCalculator.calculatePoints(winnerId, loserId);
        player.wonGame(ts, winnerId, gameId, points);
        player.lostGame(ts, loserId, gameId, points);
    }

    @Override
    public CompletableFuture<Void> surrender(StreamTimestamp ts, UUID playerId, UUID gameId) {
        GameState gameState = gameStateRepository.getState(gameId).orElseThrow(NoSuchGameException::new);

        if (!gameState.isPlayersTurn(playerId)) {
            throw new IllegalGameStateException("Not this player's turn");
        }

        eventBus.dispatch(PlayerEvents.class, GameEvents.class, (player, game) -> {
            recordSurrender(ts, gameId, gameState.getTurnState(), player, game);
        });

        return completedFuture(null);
    }
}
