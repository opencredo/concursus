package com.opencredo.concourse.examples;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;

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
