package com.opencredo.concursus.kotlin

import com.opencredo.concursus.domain.events.dispatching.EventBus
import com.opencredo.concursus.domain.time.StreamTimestamp
import com.opencredo.concursus.kotlin.PackageState.PackageTransitions
import com.opencredo.concursus.kotlin.ParcelEvent.*

sealed class ParcelEvent {
    class ReceivedAtDepot(val depotId: String) : ParcelEvent()
    class LoadedOntoTruck(val truckId: String) : ParcelEvent()
    class Delivered(val destinationId: String) : ParcelEvent()
    class DeliveryFailed() : ParcelEvent()
}

fun describeEvent(event: ParcelEvent): Unit = when (event) {
    is ReceivedAtDepot -> println("Received at depot: ${event.depotId}")
    is LoadedOntoTruck -> println("Loaded onto truck: ${event.truckId}")
    is Delivered -> println("Delivered to: ${event.destinationId}")
    is DeliveryFailed -> println("Delivery failed")
}

data class PackageState(val deliveryAttempts: Int, val isDelivered: Boolean, val currentLocation: String) {

    companion object PackageTransitions : Transitions<PackageState, ParcelEvent> {
        override fun initial(timestamp: StreamTimestamp, data: ParcelEvent): PackageState? = when (data) {
            is ReceivedAtDepot -> PackageState(0, false, "depot:" + data.depotId)
            else -> throw IllegalStateException("Cannot process state %s as initial state".format(data))
        }

        override fun next(previousState: PackageState, timestamp: StreamTimestamp, data: ParcelEvent): PackageState = when (data) {
            is LoadedOntoTruck -> previousState.copy(currentLocation = "truck:" + data.truckId)
            is Delivered -> previousState.copy(isDelivered = true, currentLocation = "destination:" + data.destinationId)
            is ReceivedAtDepot -> previousState.copy(currentLocation = "depot:" + data.depotId)
            is ParcelEvent.DeliveryFailed -> previousState.copy(deliveryAttempts = previousState.deliveryAttempts + 1)
        }
    }
}

fun main(args: Array<String>): Unit {
    val stateUpdater = InMemoryStateUpdater<PackageState>()
    val eventBus = EventBus.processingWith { batch -> batch.forEach { event -> println(event) } }
    val stateManager = StateManager(stateUpdater, PackageTransitions, eventBus)

    val receivedTimestamp = StreamTimestamp.now()
    val loadedTimestamp = StreamTimestamp.now()

    val currentState = PackageState.runAll(listOf(
            ReceivedAtDepot("depot1") at receivedTimestamp,
            LoadedOntoTruck("truck1") at loadedTimestamp
    ))

    println(stateManager.update("package1", ReceivedAtDepot("depot1") at StreamTimestamp.now()))
    println(stateManager.update("package1", LoadedOntoTruck("truck1") at StreamTimestamp.now()))
}
