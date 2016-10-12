package com.opencredo.concursus.kotlin

import com.opencredo.concursus.domain.events.dispatching.EventBus
import com.opencredo.concursus.domain.time.StreamTimestamp
import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiFunction

interface Transitions<S, in E : Any> {

    fun update(previousState: S?, event: TimestampedData<out E>): S? =
            if (previousState == null) initial(event.timestamp, event.data)
            else next(previousState, event.timestamp, event.data)

    fun initial(timestamp: StreamTimestamp, data: E): S?

    fun next(previousState: S, timestamp: StreamTimestamp, data: E): S

    fun runAll(events: List<TimestampedData<out E>>, initialState: S? = null): S? {
        var workingState: S? = initialState
        events.forEach { workingState = update(workingState, it) }
        return workingState
    }
}

interface StateUpdater<S : Any> : (String, (S?) -> S?) -> S?

class InMemoryStateUpdater<S : Any>() : StateUpdater<S> {
    override fun invoke(aggregateId: String, updater: (S?) -> S?): S? = states.compute(
            aggregateId, BiFunction { id, state -> updater(state) })

    private val states = ConcurrentHashMap<String, S>()
}

class StateManager<S : Any, E : Any>(val stateUpdater: StateUpdater<S>, val transitions: Transitions<S, E>, val eventBus: EventBus) {
    fun update(aggregateId: String, vararg events: TimestampedData<out E>): S? =
        stateUpdater.invoke(aggregateId, { s ->
            val newState = transitions.runAll(events.toList(), s)
            eventBus.dispatchTo(aggregateId, *events)
            newState
        })

}
