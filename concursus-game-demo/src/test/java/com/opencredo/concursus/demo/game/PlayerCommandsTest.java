package com.opencredo.concursus.demo.game;

import com.opencredo.concursus.demos.game.Application;
import com.opencredo.concursus.demos.game.commands.PlayerCommands;
import com.opencredo.concursus.demos.game.domain.*;
import com.opencredo.concursus.demos.game.engine.Deal;
import com.opencredo.concursus.demos.game.engine.Engine;
import com.opencredo.concursus.demos.game.engine.EngineRegistry;
import com.opencredo.concursus.demos.game.states.GameState;
import com.opencredo.concursus.demos.game.states.PlayerState;
import com.opencredo.concursus.domain.events.state.StateRepository;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.commands.methods.proxying.CommandProxyFactory;
import com.opencredo.concursus.spring.events.EventSystemBeans;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.time.Instant;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { EventSystemBeans.class, Application.class })
@WebAppConfiguration
public class PlayerCommandsTest {

    @Autowired
    public CommandProxyFactory commandBus;

    @Autowired
    public EngineRegistry engineRegistry;

    @Autowired
    public StateRepository<GameState> gameStateRepository;

    @Autowired
    public StateRepository<PlayerState> playerStateRepository;

    private final Engine engine = mock(Engine.class);

    private PlayerCommands cmd;

    @Before
    public void mockEngine() {
        engineRegistry.register("0.0.1", engine);
        when(engine.deal()).thenReturn(Deal.from(Card.values()).withFirstPlayerIndex(PlayerIndex.PLAYER_1));
        cmd = commandBus.getProxy(PlayerCommands.class);
    }

    private StreamTimestamp now() {
        return StreamTimestamp.of("test", Instant.now());
    }

    @Test
    public void playAGame() {
        String playerOneId = cmd.create(now(), "id1", "Player 1");
        String playerTwoId = cmd.create(now(), "id2", "Player 2");

        String gameId = cmd.startGame(now(), playerOneId, "0.0.1");

        cmd.joinGame(now(), playerTwoId, gameId);

        gameContinuesAfter(Card.ANGEL_SUMMONER, Card.CHAOS_BADGER);
        victoryAfter(Card.NECROTIC_TOXICITY);

        cmd.playTurn(now(), playerOneId, gameId, Card.ANGEL_SUMMONER, Optional.of(BoardSlot.of(3, BoardRow.PLAYER)));
        cmd.playTurn(now(), playerTwoId, gameId, Card.CHAOS_BADGER, Optional.of(BoardSlot.of(2, BoardRow.PLAYER)));
        cmd.playTurn(now(), playerOneId, gameId, Card.NECROTIC_TOXICITY, Optional.empty());

        PlayerState playerOneState = playerStateRepository.getState(playerOneId).get();
        PlayerState playerTwoState = playerStateRepository.getState(playerTwoId).get();

        assertThat(playerOneState.getRating(), equalTo(50));
        assertThat(playerTwoState.getRating(), equalTo(-50));
    }

    @SuppressWarnings("unchecked")
    private void gameContinuesAfter(Card...cards) {
        for (Card card : cards){
            when(engine.applyTurn(any(PlayerIndex.class), any(BoardState.class), eq(card), any(Optional.class)))
                    .thenReturn(TurnLog.withOutcome(Outcome.GAME_CONTINUES));
        }
    }

    @SuppressWarnings("unchecked")
    private void victoryAfter(Card...cards) {
        for (Card card : cards){
            when(engine.applyTurn(any(PlayerIndex.class), any(BoardState.class), eq(card), any(Optional.class)))
                    .thenReturn(TurnLog.withOutcome(Outcome.VICTORY));
        }
    }



}
