/**
 * Provides the {@link com.opencredo.concursus.domain.events.matching.EventTypeMatcher} interface, for classes that are
 * able to match {@link com.opencredo.concursus.domain.events.EventType}s to
 * {@link com.opencredo.concursus.data.tuples.TupleSchema}s.
 * <p>
 *     This is an essential part of the way Concursus bridges between the persistence / serialisation layers and the
 *     Java type system. An {@link com.opencredo.concursus.domain.events.matching.EventTypeMatcher} makes it possible
 *     to take a labelled collection of serialised values and convert it into a
 *     {@link com.opencredo.concursus.data.tuples.Tuple} validated against a
 *     {@link com.opencredo.concursus.data.tuples.TupleSchema} which states what values must be present and what types
 *     they must be deserialisable to.
 * </p>
 */
package com.opencredo.concursus.domain.events.matching;