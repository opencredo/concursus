package com.opencredo.concursus.kotlin

import com.opencredo.concursus.domain.events.dispatching.EventBus
import com.opencredo.concursus.domain.events.processing.EventBatchProcessor
import com.opencredo.concursus.domain.events.sourcing.EventSource
import com.opencredo.concursus.domain.events.storage.InMemoryEventStore
import com.opencredo.concursus.domain.time.StreamTimestamp
import com.opencredo.concursus.kotlin.LightbulbEvent.*
import com.opencredo.concursus.kotlin.LightbulbState.LightbulbTransitions
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.HOURS
import java.time.temporal.ChronoUnit.MILLIS
import java.util.*

@HandlesEventsFor("lightbulb")
sealed class LightbulbEvent {

    companion object Factory : KEventFactory<LightbulbEvent>()

    @Initial class Created(val wattage: Int) : LightbulbEvent()
    class ScrewedIn(val location: String) : LightbulbEvent()
    class Unscrewed() : LightbulbEvent()
    class SwitchedOn() : LightbulbEvent()
    class SwitchedOff() : LightbulbEvent()
}

data class LightbulbState(val wattage: Int, val location: String?, val isSwitchedOn: Boolean,
                          val switchedOnAt: Instant?, val millisecondsActive: Long) {
    companion object LightbulbTransitions : Transitions<LightbulbState, LightbulbEvent> {

        override fun initial(timestamp: StreamTimestamp, data: LightbulbEvent): LightbulbState? = when(data) {
            is Created -> LightbulbState(data.wattage, null, false, null, 0)
            else -> null
        }

        override fun next(
                previousState: LightbulbState,
                timestamp: StreamTimestamp,
                data: LightbulbEvent): LightbulbState = when(data) {
            is Created -> previousState
            is ScrewedIn -> previousState.copy(location = data.location)
            is Unscrewed -> switchedOff(previousState.copy(location = null), timestamp)
            is SwitchedOn -> switchedOn(previousState, timestamp)
            is SwitchedOff -> switchedOff(previousState, timestamp)
        }

        private fun switchedOn(state: LightbulbState, timestamp: StreamTimestamp) =
                if (state.isSwitchedOn) state
                else state.copy(isSwitchedOn = true, switchedOnAt = timestamp.timestamp)

        private fun switchedOff(state: LightbulbState, timestamp: StreamTimestamp): LightbulbState =
            if (!state.isSwitchedOn) state
            else state.copy(
                    isSwitchedOn = false,
                    switchedOnAt = null,
                    millisecondsActive = state.millisecondsActive +
                            millisSwitchedOn(state, timestamp.timestamp))

        private fun millisSwitchedOn(state: LightbulbState, timestamp: Instant): Long =
                state.switchedOnAt?.let { Duration.between(it, timestamp).toMillis() } ?: 0
    }

    fun millisecondsActiveAt(time: Instant): Long = millisecondsActive + millisSwitchedOn(this, time)

    fun kwhAt(time: Instant): Double =
            wattage.toDouble() / 1000 * (millisecondsActiveAt(time).toDouble() / 3600000)
}

fun main(args: Array<String>) {
    val eventStore = InMemoryEventStore.empty()
    val eventBus = EventBus.processingWith(EventBatchProcessor.forwardingTo(eventStore))
    val eventSource = EventSource.retrievingWith(eventStore)

    val lightbulbId = UUID.randomUUID()
    var start = StreamTimestamp.now()

    eventBus.dispatch(LightbulbEvent.Factory) {
        write(start.plus(1, MILLIS), lightbulbId, Created(wattage = 100))
        write(start,                 lightbulbId, ScrewedIn(location = "hallway"))
        write(start.plus(2, MILLIS), lightbulbId, SwitchedOn())
        write(start.plus(1, HOURS),  lightbulbId, SwitchedOff())
        write(start.plus(3, HOURS),  lightbulbId, SwitchedOn())
    }

    val cached = eventSource.preload(LightbulbEvent::class, arrayListOf(lightbulbId))

    val messages = cached.replaying(lightbulbId)
            .inAscendingCausalOrder()
            .collectAll { kevent ->
        val data = kevent.data
        val msg = when(data) {
            is Created -> "Lightbulb created with wattage " + data.wattage
            is ScrewedIn -> "Lightbulb screwed in @ " + data.location
            is Unscrewed -> "Lightbulb unscrewed"
            is SwitchedOn -> "Lightbulb switched on"
            is SwitchedOff -> "Lightbulb switched off"
        }
        msg + " at " + kevent.timestamp.timestamp
    }

    messages.forEach { println(it) }

    val state = cached.replaying(lightbulbId).buildState(LightbulbTransitions)

    println(state)

    println("Usage after 4 hours: " + state!!.kwhAt(start.plus(4, HOURS).timestamp) + " kw/h")
}