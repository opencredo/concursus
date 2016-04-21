package com.opencredo.concursus.kotlin

import com.opencredo.concursus.domain.events.dispatching.EventBus
import com.opencredo.concursus.domain.events.processing.EventBatchProcessor
import com.opencredo.concursus.domain.events.sourcing.EventSource
import com.opencredo.concursus.domain.events.storage.InMemoryEventStore
import com.opencredo.concursus.domain.time.StreamTimestamp
import com.opencredo.concursus.kotlin.LightbulbEvent.*
import java.time.temporal.ChronoUnit.MILLIS
import java.util.*

@HandlesEventsFor("lightbulb")
sealed class LightbulbEvent {

    companion object Factory : KEventFactory<LightbulbEvent>()

    class Created(val wattage: Int) : LightbulbEvent()
    class ScrewedIn(val location: String) : LightbulbEvent()
    class Unscrewed() : LightbulbEvent()
    class SwitchedOn() : LightbulbEvent()
    class SwitchedOff() : LightbulbEvent()
    class Blown(): LightbulbEvent()
}

fun main(args: Array<String>) {
    val eventStore = InMemoryEventStore.empty()
    val eventBus = EventBus.processingWith(EventBatchProcessor.forwardingTo(eventStore))
    val eventSource = EventSource.retrievingWith(eventStore)

    val lightbulbId = UUID.randomUUID()
    var start = StreamTimestamp.now()

    eventBus.dispatch(LightbulbEvent.Factory, {
        it.write(start,                 lightbulbId, Created(wattage = 40))
          .write(start.plus(1, MILLIS), lightbulbId, ScrewedIn(location = "hallway"))
          .write(start.plus(2, MILLIS), lightbulbId, SwitchedOn())
    })

    eventSource.getEvents(LightbulbEvent::class, lightbulbId).forEach { kevent ->
        val data = kevent.data
        when(data) {
            is LightbulbEvent.Created -> println("Lightbulb created with wattage " + data.wattage)
            is LightbulbEvent.ScrewedIn -> println("Lightbulb screwed in @ " + data.location)
            is LightbulbEvent.Unscrewed -> println("Lightbulb unscrewed")
            is LightbulbEvent.SwitchedOn -> println("Lightbulb switched on")
            is LightbulbEvent.SwitchedOff -> println("Lightbulb switched off")
            is LightbulbEvent.Blown -> println("Lightbulb blown")
        }
    }
}
