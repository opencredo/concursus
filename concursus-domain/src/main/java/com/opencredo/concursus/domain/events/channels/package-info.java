/**
 * Event channels are how {@link com.opencredo.concursus.domain.events.Event}s enter and leave the system.
 * <p>
 *     They provide an integration point for adapters to message queues, ESBs etc: to integrate a 3rd-party transport for
 *     events, create suitable {@link com.opencredo.concursus.domain.events.channels.EventInChannel} (or
 *     {@link com.opencredo.concursus.domain.events.channels.EventsInChannel}) and
 *     {@link com.opencredo.concursus.domain.events.channels.EventOutChannel} (or
 *     {@link com.opencredo.concursus.domain.events.channels.EventsOutChannel}) implementations for it.
 * </p>
 * <p>
 *     Events received through an {@link com.opencredo.concursus.domain.events.channels.EventsInChannel} will typically
 *     be passed on to an {@link com.opencredo.concursus.domain.events.dispatching.EventBus} for further processing.
 *     Conversely, an {@link com.opencredo.concursus.domain.events.dispatching.EventBus} can be configured to dispatch
 *     events to any {@link com.opencredo.concursus.domain.events.channels.EventsOutChannel}.
 * </p>
 */
package com.opencredo.concursus.domain.events.channels;