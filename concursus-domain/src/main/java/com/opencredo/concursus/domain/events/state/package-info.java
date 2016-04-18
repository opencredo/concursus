/**
 * Defines the {@link com.opencredo.concursus.domain.events.state.StateBuilder} interface, which represents a class
 * which can build an instance of a state class from an aggregate's event history, and the
 * {@link com.opencredo.concursus.domain.events.state.StateRepository} interface, which represents a class that can
 * retrieve state objects by aggregate id. Provides the
 * {@link com.opencredo.concursus.domain.events.state.EventSourcingStateRepository} implementation, which fetches events
 * from an {@link com.opencredo.concursus.domain.events.sourcing.EventSource} and builds state objects using a suitable
 * {@link com.opencredo.concursus.domain.events.state.StateBuilder}.
 */
package com.opencredo.concursus.domain.events.state;