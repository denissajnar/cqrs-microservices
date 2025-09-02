package dev.denissajnar.shared.events

import dev.denissajnar.shared.model.Status
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * Base interface for all domain events in the CQRS system
 */
interface DomainEvent {
    val eventId: String
    val timestamp: Instant
    val messageId: String
}

/**
 * Enum representing the type of operation performed on an order
 */
enum class OrderOperationType {
    CREATE,
    UPDATE,
    DELETE
}

/**
 * Unified event for all order operations (create, update, delete)
 * This event triggers the corresponding operation in the read model on the query side
 *
 * Field requirements by operation type:
 * - CREATE: orderId, customerId, totalAmount, status are required
 * - UPDATE: orderId is required, other fields are optional (only non-null values will be updated)
 * - DELETE: orderId, customerId, totalAmount, status are required (for audit purposes)
 */
data class OrderEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val messageId: String,
    val operationType: OrderOperationType,
    val orderId: String,
    val customerId: Long? = null,
    val totalAmount: BigDecimal? = null,
    val status: Status? = null,
    override val timestamp: Instant = Instant.now(),
) : DomainEvent {

    init {
        when (operationType) {
            OrderOperationType.CREATE -> {
                require(customerId != null) { "customerId is required for CREATE operation" }
                require(totalAmount != null) { "totalAmount is required for CREATE operation" }
                require(status != null) { "status is required for CREATE operation" }
            }

            OrderOperationType.UPDATE -> {
                require(customerId != null || totalAmount != null || status != null) {
                    "At least one field (customerId, totalAmount, or status) must be provided for UPDATE operation"
                }
            }

            OrderOperationType.DELETE -> {
                require(customerId != null) { "customerId is required for DELETE operation" }
                require(totalAmount != null) { "totalAmount is required for DELETE operation" }
                require(status != null) { "status is required for DELETE operation" }
            }
        }
    }
}
