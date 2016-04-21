package com.opencredo.concursus.kotlin

import com.opencredo.concursus.domain.time.StreamTimestamp
import java.util.*

@HandlesEventsFor("lightbulb")
sealed class LightbulbEvent {

    companion object Factory : KEventFactory<LightbulbEvent>()

    class Created(val wattage: Int) : LightbulbEvent()
    class ScrewedIn(val location: String) : LightbulbEvent()
    class Unscrewed() : LightbulbEvent()
}

fun main(args: Array<String>) {
    val events = arrayListOf(
        LightbulbEvent.create(StreamTimestamp.now(), UUID.randomUUID(), LightbulbEvent.Created(40)),
        LightbulbEvent.create(StreamTimestamp.now(), UUID.randomUUID(), LightbulbEvent.ScrewedIn("hallway")),
        LightbulbEvent.create(StreamTimestamp.now(), UUID.randomUUID(), LightbulbEvent.Unscrewed())
    )

    events.forEach { println(it.toString()) }

    val kevents: List<KEvent<LightbulbEvent>> = events.map{ KEventTypeSet.forClass(LightbulbEvent::class).fromEvent(it) }

    kevents.forEach { kevent ->
        val data = kevent.data
        when(data) {
            is LightbulbEvent.Created -> println("Lightbulb created with wattage " + data.wattage)
            is LightbulbEvent.ScrewedIn -> println("Lightbulb screwed in @ " + data.location)
            is LightbulbEvent.Unscrewed -> println("Lightbulb unscrewed")
        }
    }
}
