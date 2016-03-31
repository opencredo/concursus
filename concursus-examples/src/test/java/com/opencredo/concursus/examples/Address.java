package com.opencredo.concursus.examples;

import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;

import java.util.UUID;

public class Address {

    @HandlesEventsFor("address")
    public interface Events {
        void created(StreamTimestamp ts, UUID addressId, String[] addressLines);
        void personMovedIn(StreamTimestamp ts, UUID addressId, UUID personId);
        void personMovedOut(StreamTimestamp ts, UUID addressId, UUID personId);
        void deleted(StreamTimestamp ts, UUID personId);
    }
}
