package com.opencredo.concourse.mapping.events.methods.reflection.interpreting.api;

import com.opencredo.concourse.domain.events.Event;

public interface MethodArgumentsInterpreter {

    Event mapArguments(Object[] args);

}
