/**
 * Provides the {@link com.opencredo.concursus.domain.events.history.EventHistoryFetcher}, which may be used to retrieve
 * the event histories of one or more aggregates from an
 * {@link com.opencredo.concursus.domain.events.sourcing.EventSource}.
 * <p>
 *   This will normally only be useful when the entire history is wanted (e.g. for display to a client interested in
 *   auditing it) - the {@link com.opencredo.concursus.domain.events.sourcing.EventSource} interface itself provides a
 *   much wider range of ways of accessing, ordering and replaying aggregates' event histories.
 * </p>
 */
package com.opencredo.concursus.domain.events.history;