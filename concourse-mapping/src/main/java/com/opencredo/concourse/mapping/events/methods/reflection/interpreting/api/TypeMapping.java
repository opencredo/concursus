package com.opencredo.concourse.mapping.events.methods.reflection.interpreting.api;

import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.domain.events.EventType;

public interface TypeMapping {

    EventType getEventType();
    TupleSchema getTupleSchema();
    int getCausalOrder();

}
