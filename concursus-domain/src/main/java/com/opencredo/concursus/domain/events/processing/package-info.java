/**
 * When an {@link com.opencredo.concursus.domain.events.batching.EventBatch} completes, it is sent to an
 * {@link com.opencredo.concursus.domain.events.processing.EventBatchProcessor} (interface provided by this package).
 * <p>
 *     This package also provides a
 *     {@link com.opencredo.concursus.domain.events.processing.PublishingEventBatchProcessor}, which combines the
 *     operations of writing events to an {@link com.opencredo.concursus.domain.events.logging.EventLog} and then
 *     forwarding them to an {@link com.opencredo.concursus.domain.events.publishing.EventPublisher}.
 * </p>
 */
package com.opencredo.concursus.domain.events.processing;