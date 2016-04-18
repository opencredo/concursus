/**
 * Provides the {@link com.opencredo.concursus.domain.events.batching.EventBatch} interface, which represents a
 * collection of {@link com.opencredo.concursus.domain.events.Event}s that are to be processed together, and two
 * concrete implementations, {@link com.opencredo.concursus.domain.events.batching.ProcessingEventBatch} (which sends
 * batched events to an {@link com.opencredo.concursus.domain.events.processing.EventBatchProcessor} on completion) and
 * {@link com.opencredo.concursus.domain.events.batching.BufferingEventBatch} (which stores up events for replaying to
 * an interested observer on completion, before passing the batch on to some other
 * {@link com.opencredo.concursus.domain.events.channels.EventsOutChannel}).
 */
package com.opencredo.concursus.domain.events.batching;