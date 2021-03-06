package com.opencredo.concursus.spring.commands.processing;

import com.opencredo.concursus.mapping.annotations.HandlesCommandsFor;

import java.util.stream.Stream;

class CommandProcessorReflection {

    static Class<?> getHandlerInterface(Object commandHandler) {
        return Stream.of(commandHandler.getClass().getInterfaces())
                .filter(iface -> iface.isAnnotationPresent(HandlesCommandsFor.class))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No command handling interface found for " + commandHandler.getClass()));
    }

}
