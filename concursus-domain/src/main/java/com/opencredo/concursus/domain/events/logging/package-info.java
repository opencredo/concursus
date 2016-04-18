/**
 * Provides the {@link com.opencredo.concursus.domain.events.logging.EventLog} interface, which represents an object
 * that will assign a processing id to each {@link com.opencredo.concursus.domain.events.Event} in a collection of
 * events, write them to persistent storage and then return a list of processed events (from which duplicates may have
 * been removed).
 */
package com.opencredo.concursus.domain.events.logging;