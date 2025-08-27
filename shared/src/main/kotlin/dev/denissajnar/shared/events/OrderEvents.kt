package dev.denissajnar.shared.events

import dev.denissajnar.shared.model.Status
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * Base interface for all domain events in the CQRS system
 */
interface DomainEvent {
    val eventId: Long
    val timestamp: Instant
}

/**
 * Event published when a new order is created in the command side
 * This event triggers the creation of the read model in the query side
 */
data class OrderCreatedEvent(
    override val eventId: Long = Random().nextLong(),
    val historyId: String,
    val customerId: Long,
    val totalAmount: BigDecimal,
    val status: Status = Status.PENDING,
    override val timestamp: Instant = Instant.now(),
) : DomainEvent

/**
 * Event published when an order is updated in the command side
 * This event triggers the update of the read model in the query side
 */
data class OrderUpdatedEvent(
    override val eventId: Long = Random().nextLong(),
    val historyId: String,
    val customerId: Long,
    val totalAmount: BigDecimal,
    val status: Status,
    override val timestamp: Instant = Instant.now(),
) : DomainEvent

/**
 * Event published when an order is deleted in the command side
 * This event triggers the deletion of the read model in the query side
 */
data class OrderDeletedEvent(
    override val eventId: Long = Random().nextLong(),
    val historyId: String,
    val orderId: String,
    override val timestamp: Instant = Instant.now(),
) : DomainEvent
