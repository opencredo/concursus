package com.opencredo.concursus.demos.game.events;

import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;
import com.opencredo.concursus.mapping.annotations.Initial;
import com.opencredo.concursus.mapping.annotations.Terminal;

import java.util.UUID;

@HandlesEventsFor("player")
public interface PlayerEvents {

    @Initial
    void created(StreamTimestamp ts, UUID playerId, String playerName);
    void changedName(StreamTimestamp ts, UUID playerId, String newPlayerName);

    void startedGame(StreamTimestamp ts, UUID playerId, UUID gameId);
    void joinedGame(StreamTimestamp ts, UUID playerId, UUID gameId);

    void wonGame(StreamTimestamp ts, UUID playerId, UUID gameId, int ratingIncrease);
    void lostGame(StreamTimestamp ts, UUID playerId, UUID gameId, int ratingDecrease);

    @Terminal
    void deleted(StreamTimestamp ts, UUID playerId);
}
