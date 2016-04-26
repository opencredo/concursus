package com.opencredo.concursus.examples;

import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;

public class Address {

    @HandlesEventsFor("address")
    public interface Events {
        void created(StreamTimestamp ts, String addressId, String[] addressLines);
        void personMovedIn(StreamTimestamp ts, String addressId, String personId);
        void personMovedOut(StreamTimestamp ts, String addressId, String personId);
        void deleted(StreamTimestamp ts, String personId);
    }
}
