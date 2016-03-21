package com.opencredo.concourse.mapping.events.methods.history;

import com.opencredo.concourse.domain.events.history.EventHistoryFetcher;
import com.opencredo.concourse.mapping.events.methods.reflection.EmitterInterfaceInfo;

/**
 * Utility class that constructs an {@link EventHistoryFetcher} using the type binding and event ordering information
 * encoded in an event-emitter interface.
 */
public final class MappingEventHistoryFetcher {

    /**
     * Constructs a new {@link EventHistoryFetcher} using the type binding and event ordering information
     * encoded in the supplied event-emitter interface.
     * @param eventInterface The event-emitter interface to reflect over.
     * @param <T> The type of the event-emitter interface.
     * @return The constructed {@link EventHistoryFetcher}.
     */
    public static <T> EventHistoryFetcher mapping(Class<? extends T> eventInterface) {
        EmitterInterfaceInfo<T> interfaceInfo = EmitterInterfaceInfo.forInterface(eventInterface);

        return EventHistoryFetcher.using(
                interfaceInfo.getEventTypeBinding(),
                interfaceInfo.getCausalOrderComparator());
    }

}
