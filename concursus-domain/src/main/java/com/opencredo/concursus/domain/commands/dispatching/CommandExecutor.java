package com.opencredo.concursus.domain.commands.dispatching;

import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandResult;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public interface CommandExecutor extends BiConsumer<Command, CompletableFuture<CommandResult>> {
}
