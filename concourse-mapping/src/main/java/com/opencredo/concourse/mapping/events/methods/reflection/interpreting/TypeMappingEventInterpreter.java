package com.opencredo.concourse.mapping.events.methods.reflection.interpreting;

import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.api.EventInterpreter;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.api.TypeMapping;

public final class TypeMappingEventInterpreter implements EventInterpreter, TypeMapping {

    public static TypeMappingEventInterpreter create(EventInterpreter eventInterpreter, EventType eventType, TupleSchema tupleSchema, int causalOrder) {
        return new TypeMappingEventInterpreter(eventInterpreter, eventType, tupleSchema, causalOrder);
    }

    private final EventInterpreter eventInterpreter;

    private final EventType eventType;
    private final TupleSchema tupleSchema;
    private final int causalOrder;

    private TypeMappingEventInterpreter(EventInterpreter eventInterpreter, EventType eventType, TupleSchema tupleSchema, int causalOrder) {
        this.eventInterpreter = eventInterpreter;
        this.eventType = eventType;
        this.tupleSchema = tupleSchema;
        this.causalOrder = causalOrder;
    }

    @Override
    public Object[] mapEvent(Event event) {
        return eventInterpreter.mapEvent(event);
    }

    @Override
    public EventType getEventType() {
        return eventType;
    }

    @Override
    public TupleSchema getTupleSchema() {
        return tupleSchema;
    }

    @Override
    public int getCausalOrder() {
        return causalOrder;
    }
}
