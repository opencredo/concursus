/**
 * Provides filter interfaces for various pieces of event-handling middleware.
 * <p>
 *     A filter on an interface <code>T</code> is typically a <code>UnaryOperator&lt;T&gt;</code>, which takes an
 *     existing <code>T</code> and returns the filtered <code>T</code>.
 * </p>
 * <p>
 *     Filters provide general purpose hooks for observing and intercepting different stages of an event processing
 *     pipeline, and are used for purpose such as:
 * </p>
 *     <ul>
 *         <li>Adding logging.</li>
 *         <li>Detecting and removing duplicate events.</li>
 *         <li>Modifying the way parts of the event-handling chain are executed (e.g. asynchronous event publishing).</li>
 *         <li>Handling and recovering from failures, rather than propagating exceptions back up the middleware chain.</li>
 *     </ul>
 *
 */
package com.opencredo.concursus.domain.events.filtering;