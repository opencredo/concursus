package com.opencredo.concursus.demos.game.events;

import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;
import com.opencredo.concursus.mapping.annotations.Initial;
import com.opencredo.concursus.mapping.annotations.Terminal;

@HandlesEventsFor("player")
public interface PlayerEvents {

    @Initial
    void created(StreamTimestamp ts, String playerId, String playerName);
    void changedName(StreamTimestamp ts, String playerId, String newPlayerName);

    void startedGame(StreamTimestamp ts, String playerId, String gameId);
    void joinedGame(StreamTimestamp ts, String playerId, String gameId);

    void wonGame(StreamTimestamp ts, String playerId, String gameId, int ratingIncrease);
    void lostGame(StreamTimestamp ts, String playerId, String gameId, int ratingDecrease);

    @Terminal
    void deleted(StreamTimestamp ts, String playerId);
}
