package dev.denissajnar.query.mapper

import dev.denissajnar.query.dto.OrderDTO
import dev.denissajnar.query.entity.Order
import dev.denissajnar.shared.events.OrderCreatedEvent

/**
 * Extension function to convert OrderCreatedEvent to Order entity
 * Used when processing events from the command side
 */
fun OrderCreatedEvent.toEntity(): Order = Order(
    id = this.orderId,
    customerId = this.customerId,
    totalAmount = this.totalAmount,
    status = this.status,
    createdAt = this.timestamp
)

/**
 * Extension function to convert Order entity to OrderDTO
 * Used for API responses in the query side
 */
fun Order.toDTO(): OrderDTO = OrderDTO(
    id = requireNotNull(this.id) { "Order ID cannot be null in response" },
    customerId = this.customerId,
    totalAmount = this.totalAmount,
    status = this.status,
    createdAt = this.createdAt
)

/**
 * Extension function to convert list of Order entities to list of OrderDTOs
 * Convenient for bulk conversion in list responses
 */
fun List<Order>.toDTOs(): List<OrderDTO> = this.map { it.toDTO() }