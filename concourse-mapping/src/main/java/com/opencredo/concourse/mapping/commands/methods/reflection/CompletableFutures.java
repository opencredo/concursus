package com.opencredo.concourse.mapping.commands.methods.reflection;

import java.util.concurrent.CompletableFuture;

public final class CompletableFutures {

    private CompletableFutures() {
    }

    public static <T> CompletableFuture<T> failing(Throwable e) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(e);
        return future;
    }
}
