/**
 * Provides the {@link com.opencredo.concursus.domain.events.sourcing.EventSource} and
 * {@link com.opencredo.concursus.domain.events.sourcing.CachedEventSource} interfaces, which represent sources of
 * {@link com.opencredo.concursus.domain.events.Event}s.
 * <p>
 * Given an {@link com.opencredo.concursus.domain.events.sourcing.EventRetriever}, an
 * {@link com.opencredo.concursus.domain.events.sourcing.EventSource} can be created that will fetch events using the
 * event retriever, and optionally cache them so that they can be replayed from memory. This is useful when we want to
 * retrieve events for a group of aggregates in a single query, then replay the event history of each aggregate in the
 * group separately from memory.
 * </p>
 */
package com.opencredo.concursus.domain.events.sourcing;