package com.opencredo.concourse.mapping.events.methods.reflection.interpreting;

import com.opencredo.concourse.data.tuples.TupleSchema;
import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.EventType;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.api.EventInterpreter;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.api.MethodArgumentsInterpreter;
import com.opencredo.concourse.mapping.events.methods.reflection.interpreting.api.TypeMapping;

public final class InterfaceMethodMapping implements MethodArgumentsInterpreter, EventInterpreter, TypeMapping {

    static InterfaceMethodMapping using(MethodArgumentsInterpreter methodArgumentsInterpreter, TypeMappingEventInterpreter eventInterpreter) {
        return new InterfaceMethodMapping(methodArgumentsInterpreter, eventInterpreter);
    }

    private final MethodArgumentsInterpreter methodArgumentsInterpreter;
    private final TypeMappingEventInterpreter eventInterpreter;

    private InterfaceMethodMapping(MethodArgumentsInterpreter methodArgumentsInterpreter, TypeMappingEventInterpreter eventInterpreter) {
        this.methodArgumentsInterpreter = methodArgumentsInterpreter;
        this.eventInterpreter = eventInterpreter;
    }

    @Override
    public Event mapArguments(Object[] args) {
        return methodArgumentsInterpreter.mapArguments(args);
    }

    @Override
    public Object[] mapEvent(Event event) {
        return eventInterpreter.mapEvent(event);
    }

    @Override
    public EventType getEventType() {
        return eventInterpreter.getEventType();
    }

    @Override
    public TupleSchema getTupleSchema() {
        return eventInterpreter.getTupleSchema();
    }

    @Override
    public int getCausalOrder() {
        return eventInterpreter.getCausalOrder();
    }
}
