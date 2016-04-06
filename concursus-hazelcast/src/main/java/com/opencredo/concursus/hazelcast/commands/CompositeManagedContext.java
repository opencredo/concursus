package com.opencredo.concursus.hazelcast.commands;

import com.hazelcast.core.ManagedContext;

import java.util.Arrays;
import java.util.List;

/**
 * A {@link ManagedContext} that composes multiple {@link ManagedContext}s together, applying each in turn to each
 * object deserialised to a Hazelcast node.
 */
public final class CompositeManagedContext implements ManagedContext {

    /**
     * Construct a new {@link ManagedContext} that composes together all of the supplied {@link ManagedContext}s.
     * @param managedContexts The {@link ManagedContext}s to compose.
     * @return The compose {@link ManagedContext}.
     */
    public static ManagedContext of(ManagedContext...managedContexts) {
        return of(Arrays.asList(managedContexts));
    }

    /**
     * Construct a new {@link ManagedContext} that composes together all of the supplied {@link ManagedContext}s.
     * @param managedContexts The {@link ManagedContext}s to compose.
     * @return The compose {@link ManagedContext}.
     */
    public static ManagedContext of(List<ManagedContext> managedContexts) {
        return new CompositeManagedContext(managedContexts);
    }

    private final List<ManagedContext> managedContexts;

    private CompositeManagedContext(List<ManagedContext> managedContexts) {
        this.managedContexts = managedContexts;
    }

    @Override
    public Object initialize(Object o) {
        Object result = o;
        for (ManagedContext context : managedContexts) {
            result = context.initialize(result);
        }
        return result;
    }
}
