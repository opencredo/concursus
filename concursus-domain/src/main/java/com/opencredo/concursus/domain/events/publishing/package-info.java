/**
 * Provides the {@link com.opencredo.concursus.domain.events.publishing.EventPublisher} interface, representing an
 * object that published {@link com.opencredo.concursus.domain.events.Event}s to subscribers, and the
 * {@link com.opencredo.concursus.domain.events.publishing.EventSubscribable} interface, which represents an object
 * that event handlers can subscribe to.
 *
 * <p>The {@link com.opencredo.concursus.domain.events.publishing.SubscribableEventPublisher} implementation of both of
 * these interfaces provides a simple mechanism for publishing events to multiple subscribers.</p>
 */
package com.opencredo.concursus.domain.events.publishing;