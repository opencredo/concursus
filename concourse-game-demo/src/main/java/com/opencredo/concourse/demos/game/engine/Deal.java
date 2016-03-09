package com.opencredo.concourse.demos.game.engine;

import com.opencredo.concourse.demos.game.domain.Card;
import com.opencredo.concourse.demos.game.domain.PlayerIndex;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Deal {

    public static Deal from(Card[] cards) {
        Random random = new Random();
        Map<Card, Integer> cardOrder = Stream.of(cards).collect(Collectors.toMap(Function.identity(), card -> random.nextInt()));
        Stack<Card> cardStack = cardOrder.entrySet().stream()
                .sorted(Comparator.comparing(Entry::getValue))
                .map(Entry::getKey)
                .collect(Collectors.toCollection(Stack::new));

        List<Card> playerOneCards = new ArrayList<>();
        List<Card> playerTwoCards = new ArrayList<>();

        for (int i=0; i < 10; i++) {
            playerOneCards.add(cardStack.pop());
            playerTwoCards.add(cardStack.pop());
        }

        PlayerIndex firstPlayerIndex = PlayerIndex.values()[new Random().nextInt(2)];

        return new Deal(firstPlayerIndex, playerOneCards, playerTwoCards);
    }

    private final PlayerIndex firstPlayerIndex;
    private final List<Card> playerOneCards;
    private final List<Card> playerTwoCards;

    private Deal(PlayerIndex firstPlayerIndex, List<Card> playerOneCards, List<Card> playerTwoCards) {
        this.firstPlayerIndex = firstPlayerIndex;
        this.playerOneCards = playerOneCards;
        this.playerTwoCards = playerTwoCards;
    }

    public List<Card> getPlayerOneCards() {
        return playerOneCards;
    }

    public List<Card> getPlayerTwoCards() {
        return playerTwoCards;
    }

    public PlayerIndex getFirstPlayerIndex() {
        return firstPlayerIndex;
    }

    public Deal withFirstPlayerIndex(PlayerIndex firstPlayerIndex) {
        return new Deal(firstPlayerIndex, playerOneCards, playerTwoCards);
    }
}
