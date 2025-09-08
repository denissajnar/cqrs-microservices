package dev.denissajnar.query.mapper

import dev.denissajnar.query.dto.OrderQueryResponse
import dev.denissajnar.query.entity.OrderQuery

/**
 * Extension function to convert OrderQuery entity to OrderQueryResponse
 * Used for API responses in the query side
 */
fun OrderQuery.toResponse(): OrderQueryResponse = OrderQueryResponse(
    id = requireNotNull(this.id) { "OrderQuery ID cannot be null in response" },
    orderId = requireNotNull(this.orderId) { "OrderQuery orderId cannot be null in response" },
    customerId = this.customerId,
    totalAmount = this.totalAmount,
    status = this.status,
    createdAt = this.createdAt,
)

/**
 * Extension function to convert list of OrderQuery entities to list of OrderDTOs
 * Convenient for bulk conversion in list responses
 */
fun List<OrderQuery>.toResponses(): List<OrderQueryResponse> = this.map { it.toResponse() }
