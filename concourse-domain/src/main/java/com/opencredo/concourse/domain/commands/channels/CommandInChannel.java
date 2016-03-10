package com.opencredo.concourse.domain.commands.channels;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface CommandInChannel<I, O> extends Function<I, CompletableFuture<O>> {

}
