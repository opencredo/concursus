package com.opencredo.concourse.domain.commands.dispatching;

import com.opencredo.concourse.domain.commands.Command;
import com.opencredo.concourse.domain.commands.CommandResult;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public interface CommandExecutor extends BiConsumer<Command, CompletableFuture<CommandResult>> {
}
