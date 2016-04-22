package com.opencredo.concursus.kotlin

import com.opencredo.concursus.domain.common.AggregateId
import com.opencredo.concursus.domain.events.Event
import com.opencredo.concursus.domain.events.dispatching.EventBus
import com.opencredo.concursus.domain.events.sourcing.CachedEventSource
import com.opencredo.concursus.domain.events.sourcing.EventReplayer
import com.opencredo.concursus.domain.events.sourcing.EventSource
import com.opencredo.concursus.domain.time.TimeRange
import java.util.*
import kotlin.reflect.KClass

/**
 * Extension methods to core domain classes, supporting Kotlin mappings
 */

fun <E : Any> Event.toKEvent(eventSuperclass: KClass<E>): KEvent<E> = KEventTypeSet.forClass(eventSuperclass).fromEvent(this)

fun <E : Any> EventBus.dispatch(factory: KEventFactory<E>, writeEvents: KEventWriter<E>.() -> Unit): Unit =
        this.dispatch { batch -> factory.writingTo { batch.accept( it ) }.writeEvents() }

fun <E : Any> EventSource.getEvents(
        eventClass: KClass<E>,
        aggregateId: UUID,
        timeRange: TimeRange = TimeRange.unbounded()): List<KEvent<E>> =
    KEventTypeSet.forClass(eventClass).let { ets ->
        this.getEvents(
                ets.eventTypeMatcher,
                AggregateId.of(ets.aggregateType, aggregateId),
                timeRange)
                .map { ets.fromEvent(it) }
    }


final class KEventReplayer<E : Any>(val eventTypeSet: KEventTypeSet<E>, val replayer: EventReplayer) {

    fun inAscendingOrder(): KEventReplayer<E> = KEventReplayer(eventTypeSet, replayer.inAscendingOrder())

    fun inAscendingOrder(order: Comparator<Event>): KEventReplayer<E> =
            KEventReplayer(eventTypeSet, replayer.inAscendingOrder(order))

    fun inAscendingCausalOrder(): KEventReplayer<E> = inAscendingOrder(eventTypeSet.causalOrder)

    fun inDescendingOrder(): KEventReplayer<E> = KEventReplayer(eventTypeSet, replayer.inDescendingOrder())

    fun inDescendingOrder(order: Comparator<Event>): KEventReplayer<E> =
            KEventReplayer(eventTypeSet, replayer.inDescendingOrder(order))

    fun inDescendingCausalOrder(): KEventReplayer<E> = inDescendingOrder(eventTypeSet.causalOrder)

    fun filtering(predicate: (Event) -> Boolean): KEventReplayer<E> =
            KEventReplayer(eventTypeSet, replayer.filter(predicate))

    fun replayAll(handler: (KEvent<E>) -> Unit): Unit = replayer.replayAll { handler(eventTypeSet.fromEvent(it)) }

    fun replayFirst(handler: (KEvent<E>) -> Unit): Unit = replayer.replayFirst { handler(eventTypeSet.fromEvent(it)) }

    fun <T> collectAll(collector: (KEvent<E>) -> T): List<T> = replayer.toList()
            .map { collector(eventTypeSet.fromEvent(it)) }

    fun <T> collectFirst(collector: (KEvent<E>) -> T): T? =
        replayer.toList().first()?.let { collector(eventTypeSet.fromEvent(it)) }

    fun toList(): List<KEvent<E>> = replayer.toList().map { eventTypeSet.fromEvent(it) }

    fun <S> buildState(transitions: Transitions<S, E>, initialState: S? = null): S? =
            transitions.runAll(inAscendingCausalOrder().toList(), initialState)
}

fun <E : Any> EventSource.replaying(
        eventClass: KClass<E>,
        aggregateId: UUID,
        timeRange: TimeRange = TimeRange.unbounded()): KEventReplayer<E> =
    KEventTypeSet.forClass(eventClass).let {
    return KEventReplayer(
            it,
            this.replaying(
                    it.eventTypeMatcher,
                    AggregateId.of(it.aggregateType, aggregateId),
                    timeRange))
    }

final class KCachedEventSource<E: Any>(val eventSource: CachedEventSource, val eventTypeSet: KEventTypeSet<E>) {

    fun getEvents(aggregateId: UUID, timeRange: TimeRange = TimeRange.unbounded()): List<KEvent<E>> =
        eventSource.getEvents(AggregateId.of(eventTypeSet.aggregateType, aggregateId))
            .map { eventTypeSet.fromEvent(it) }

    fun replaying(aggregateId: UUID, timeRange: TimeRange = TimeRange.unbounded()): KEventReplayer<E> =
            KEventReplayer(eventTypeSet, eventSource.replaying(
                    AggregateId.of(eventTypeSet.aggregateType, aggregateId), timeRange))
}

fun <E: Any> EventSource.preload(
        eventClass: KClass<E>,
        aggregateIds: List<UUID>,
        timeRange: TimeRange = TimeRange.unbounded()) : KCachedEventSource<E> =
        KEventTypeSet.forClass(eventClass).let {
            KCachedEventSource(
                this.preload(it.eventTypeMatcher, it.aggregateType, aggregateIds, timeRange),
                it)
        }

