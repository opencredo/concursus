package com.opencredo.concursus.kotlin

import com.opencredo.concursus.data.tuples.Tuple
import com.opencredo.concursus.data.tuples.TupleKey
import com.opencredo.concursus.data.tuples.TupleSchema
import com.opencredo.concursus.data.tuples.TupleSlot
import com.opencredo.concursus.domain.common.AggregateId
import com.opencredo.concursus.domain.common.VersionedName
import com.opencredo.concursus.domain.events.Event
import com.opencredo.concursus.domain.events.EventCharacteristics
import com.opencredo.concursus.domain.events.EventType
import com.opencredo.concursus.domain.events.matching.EventTypeMatcher
import com.opencredo.concursus.domain.time.StreamTimestamp
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.*
import kotlin.reflect.jvm.javaType

annotation class HandlesEventsFor(val value: String)
annotation class Name(val value: String, val version: String = "0")
annotation class Initial
annotation class Terminal
annotation class Order(val value: Int)

data class KEvent<E : Any>(val timestamp: StreamTimestamp, val aggregateId : String, val data: E) {
    fun toEvent() : Event =
            KEventTypeSet.forClass((data.javaClass.superclass as Class<E>).kotlin)
                    .toEvent(timestamp, aggregateId, data)
}

final class KEventWriter<E: Any>(val factory: KEventFactory<E>, val receiver: (Event) -> Unit) {
    fun write(timestamp: StreamTimestamp, aggregateId: String, data: E): KEventWriter<E> {
        receiver(factory.create(timestamp, aggregateId, data))
        return this
    }
}

open class KEventFactory<E : Any> {
    fun create(timestamp: StreamTimestamp, aggregateId : String, data: E) : Event =
            KEvent(timestamp, aggregateId, data).toEvent()

    fun writingTo(receiver: (Event) -> Unit): KEventWriter<E> = KEventWriter(this, receiver)
}

final class KEventTypeSet<E : Any>(
        val aggregateType: String,
        val eventTypeByName: Map<VersionedName, KEventType<E>>,
        val eventTypeByDataClass: Map<Class<E>, KEventType<E>>,
        val eventTypeMap: Map<EventType, TupleSchema>) {

    companion object Factory {

        val cache = ConcurrentHashMap<KClass<*>, KEventTypeSet<*>>()

        fun <E : Any> forClass(eventSuperclass: KClass<E>): KEventTypeSet<E> =
                cache.computeIfAbsent(eventSuperclass, { forClassUncached(it) }) as KEventTypeSet<E>

        private fun <E : Any> forClassUncached(eventSuperclass: KClass<E>): KEventTypeSet<E> {
            val aggregateType = getAggregateType(eventSuperclass)
            val eventTypes = getEventTypes(aggregateType, eventSuperclass)

            val eventTypeByName = eventTypes.associateBy { it.eventName }
            val eventTypeByDataClass = eventTypes.associateBy { it.dataClass.java }

            val schemasByEventType = eventTypes.associateBy(
                    { EventType.of(aggregateType, it.eventName) },
                    {it.schema})

            return KEventTypeSet(aggregateType, eventTypeByName, eventTypeByDataClass, schemasByEventType)
        }

        private fun <E : Any> getAggregateType(eventSuperclass: KClass<E>) =
                (eventSuperclass.java.getAnnotation(HandlesEventsFor::class.java)?.value
                ?: eventSuperclass.java.simpleName.decapitalize().replace("Event", ""))

        private fun <E : Any> getEventTypes(
                aggregateType: String,
                eventSuperclass: KClass<E>): List<KEventType<E>> =
                eventSuperclass.nestedClasses.map { KEventType.forDataClass(it, aggregateType) as KEventType<E> }

    }

    fun toEvent(timestamp: StreamTimestamp, aggregateId: String, data: E): Event =
            eventTypeByDataClass[data.javaClass]!!.toEvent(timestamp, aggregateId, data)

    fun fromEvent(event: Event): KEvent<E> =
            eventTypeByName[event.eventName]!!.fromEvent(event)

    val eventTypeMatcher = EventTypeMatcher.matchingAgainst(eventTypeMap)

    val causalOrder = Comparator.comparing<Event, Int> { eventTypeByName[it.eventName]!!.order }
}

data class KEventType<T: Any>(
        val dataClass: KClass<T>,
        val order: Int,
        val aggregateType: String,
        val eventName: VersionedName,
        val schema: TupleSchema,
        val makeTuple: (T) -> Tuple,
        val makeInstance: (Tuple) -> T) {

    companion object Factory {

        var initial = Int.MIN_VALUE
        var terminal = Int.MAX_VALUE
        var preterminal = terminal - 1

        fun <E : Any, T : E> forDataClass(dataClass: KClass<T>, aggregateType: String): KEventType<T> {
            val eventName = getEventName(dataClass)
            val parameters = getParameters(dataClass)
            val schema = getSchema(aggregateType, eventName, parameters)
            val keysByProperty = getKeysByProperty(dataClass, schema)

            return KEventType(
                    dataClass,
                    getOrder(dataClass.java),
                    aggregateType,
                    eventName,
                    schema,
                    getDataToTupleConverter(schema, keysByProperty),
                    getTupleToDataConverter(dataClass, parameters, keysByProperty))
        }

        private fun <T : Any> getOrder(dataClass: Class<T>): Int {
            if (dataClass.isAnnotationPresent(Initial::class.java)) return initial
            if (dataClass.isAnnotationPresent(Terminal::class.java)) return terminal
            return dataClass.getAnnotation(Order::class.java)?.value ?: preterminal
        }

        private fun <T : Any> getDataToTupleConverter(
                schema: TupleSchema,
                keysByProperty: Map<KProperty1<T, *>, TupleKey<Any>>): (T) -> Tuple = { data ->
            schema.make(*keysByProperty
                    .map { it.value.of(it.key.get(data)) }
                    .toTypedArray())
        }

        private fun <T : Any> getTupleToDataConverter(
                dataClass: KClass<T>,
                parameters: List<KParameter>,
                keysByProperty: Map<KProperty1<T, *>, TupleKey<Any>>
        ): (Tuple) -> T {
            val keysByName = keysByProperty.mapKeys { it.key.name }
            val keysInParameterOrder = parameters.map { keysByName[it.name] }
            val constructor = dataClass.primaryConstructor
            return if (constructor == null) { tuple -> dataClass.objectInstance!! }
            else { tuple -> constructor.call(*keysInParameterOrder.map { tuple.get(it)}.toTypedArray() ) }
        }

        private fun <T : Any> getKeysByProperty(nestedClass: KClass<T>, schema: TupleSchema): Map<KProperty1<T, *>, TupleKey<Any>> {
            return nestedClass.memberProperties.associateBy(
                    { it },
                    { schema.getKey<Any>(it.name, it.returnType.javaType) })
        }

        private fun getSchema(aggregateType: String, eventName: VersionedName, parameters: List<KParameter>): TupleSchema {
            return TupleSchema.of(aggregateType + ":" + eventName,
                    *parameters
                            .sortedBy { parameter -> parameter.name!! }
                            .map { TupleSlot.of(it.name, it.type.javaType) }
                            .toTypedArray())
        }

        private fun getParameters(nestedClass: KClass<*>): List<KParameter> = nestedClass.primaryConstructor?.parameters
                ?: Collections.emptyList()

        private fun getEventName(nestedClass: KClass<*>): VersionedName {
            val eventNameAnnotation = nestedClass.java.getAnnotation(Name::class.java)
            return VersionedName.of(
                    eventNameAnnotation?.value ?: nestedClass.simpleName!!.decapitalize(),
                    eventNameAnnotation?.version ?: "0")
        }
    }

    private fun characteristics(): Int {
        if (order == initial) return EventCharacteristics.IS_INITIAL
        if (order == terminal) return EventCharacteristics.IS_TERMINAL
        return 0
    }

    fun toEvent(timestamp: StreamTimestamp, aggregateId: String, data: T): Event =
            Event.of(AggregateId.of(aggregateType, aggregateId), timestamp, eventName, makeTuple(data),
                    characteristics())

    fun fromEvent(event: Event): KEvent<T> =
            KEvent(event.eventTimestamp, event.aggregateId.id, makeInstance(event.parameters))
}


interface Transitions<S, E : Any> {

    fun update(previousState: S?, event: KEvent<E>): S? =
            if (previousState == null) initial(event.timestamp, event.data)
            else next(previousState, event.timestamp, event.data)

    fun initial(timestamp: StreamTimestamp, data: E): S?

    fun next(previousState: S, timestamp: StreamTimestamp, data: E): S

    fun runAll(events: List<KEvent<E>>, initialState: S? = null): S? {
        var workingState: S? = initialState
        events.forEach { workingState = update(workingState, it) }
        return workingState
    }
}