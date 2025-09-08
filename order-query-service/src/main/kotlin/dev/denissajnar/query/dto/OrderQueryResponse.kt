package dev.denissajnar.query.dto

import dev.denissajnar.shared.model.Status
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.Instant

/**
 * DTO for order query responses
 */
@Schema(description = "OrderQuery information from the query side")
data class OrderQueryResponse(

    @field:Schema(description = "Unique order identifier", example = "1")
    val id: Long,

    @field:Schema(
        description = "Unique identifier for the order assigned by the system",
        example = "68af074ac3c1775477995459",
    )
    val orderId: String,

    @field:Schema(description = "ID of the customer who placed the order", example = "1")
    val customerId: Long,

    @field:Schema(description = "Total amount of the order", example = "99.99")
    val totalAmount: BigDecimal,

    @field:Schema(description = "Current status of the order", example = "PENDING")
    val status: Status,

    @field:Schema(description = "Timestamp when the order was created", example = "2023-09-15T12:34:56Z")
    val createdAt: Instant,
)
