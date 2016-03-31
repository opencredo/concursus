package com.opencredo.concursus.mapping.commands.methods.reflection;

import com.opencredo.concursus.domain.commands.Command;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public interface CommandDispatcher extends BiFunction<Object, Command, CompletableFuture<?>> {
}
