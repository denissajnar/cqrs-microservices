package dev.denissajnar.query.mapper

import dev.denissajnar.query.dto.OrderQueryDTO
import dev.denissajnar.query.entity.OrderQuery
import dev.denissajnar.shared.events.OrderCreatedEvent
import dev.denissajnar.shared.events.OrderUpdatedEvent

/**
 * Extension function to convert OrderCreatedEvent to OrderQuery entity
 * Used when processing events from the command side
 */
fun OrderCreatedEvent.toEntity(): OrderQuery = OrderQuery(
    historyId = this.historyId,
    customerId = this.customerId,
    totalAmount = this.totalAmount,
    status = this.status,
    createdAt = this.timestamp,
)

/**
 * Extension function to convert OrderUpdatedEvent to OrderQuery entity
 * Used when processing update events from the command side
 */
fun OrderUpdatedEvent.toEntity(): OrderQuery = OrderQuery(
    historyId = this.historyId,
    customerId = this.customerId,
    totalAmount = this.totalAmount,
    status = this.status,
    createdAt = this.timestamp,
)

/**
 * Extension function to convert OrderQuery entity to OrderQueryDTO
 * Used for API responses in the query side
 */
fun OrderQuery.toDTO(): OrderQueryDTO = OrderQueryDTO(
    id = requireNotNull(this.id) { "OrderQuery ID cannot be null in response" },
    historyId = requireNotNull(this.historyId) { "OrderQuery ID cannot be null in response" },
    customerId = this.customerId,
    totalAmount = this.totalAmount,
    status = this.status,
    createdAt = this.createdAt,
)

/**
 * Extension function to convert list of OrderQuery entities to list of OrderDTOs
 * Convenient for bulk conversion in list responses
 */
fun List<OrderQuery>.toDTOs(): List<OrderQueryDTO> = this.map { it.toDTO() }
