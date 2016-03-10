package com.opencredo.concourse.mapping.commands.methods.reflection;

import com.opencredo.concourse.domain.commands.Command;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public interface CommandDispatcher extends BiFunction<Object, Command, CompletableFuture<?>> {
}
