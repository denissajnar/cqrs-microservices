package dev.denissajnar.query.dto

import dev.denissajnar.shared.model.Status
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * DTO for order query responses
 */
@Schema(description = "Order information from the query side")
data class OrderDTO(

    @field:Schema(description = "Unique order identifier", example = "60ded37c-69c2-4464-8933-10f8c453f846")
    val id: UUID,

    @field:Schema(description = "ID of the customer who placed the order", example = "1")
    val customerId: Long,

    @field:Schema(description = "Total amount of the order", example = "99.99")
    val totalAmount: BigDecimal,

    @field:Schema(description = "Current status of the order", example = "PENDING")
    val status: Status,

    @field:Schema(description = "Timestamp when the order was created")
    val createdAt: Instant
)