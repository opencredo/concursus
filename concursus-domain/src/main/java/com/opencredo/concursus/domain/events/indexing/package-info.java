/**
 * Provides the experimental {@link com.opencredo.concursus.domain.events.indexing.EventIndex} and
 * {@link com.opencredo.concursus.domain.events.indexing.EventIndexer} interfaces, for classes which index aggregate ids
 * by the most recent values assigned to parameters of events recorded against each aggregate.
 * <p>
 *     Only an {@link com.opencredo.concursus.domain.events.indexing.InMemoryEventIndex} is currently provided.
 * </p>
 */
package com.opencredo.concursus.domain.events.indexing;