package dev.denissajnar.command.dto

import dev.denissajnar.shared.model.Status
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.Instant

/**
 * DTO for creating a new order
 */
@Schema(description = "Request payload for creating a new order")
data class CreateOrderCommandDTO(

    @field:NotNull(message = "Customer ID is required")
    @field:Min(value = 1, message = "Customer ID must be positive")
    @field:Schema(description = "ID of the customer placing the order", example = "1")
    val customerId: Long,

    @field:NotNull(message = "Total amount is required")
    @field:DecimalMin(value = "0.01", message = "Total amount must be positive")
    @field:Schema(description = "Total amount of the order", example = "99.99")
    val totalAmount: BigDecimal,
)


/**
 * DTO for updating an existing order
 */
@Schema(description = "Request payload for updating an existing order")
data class UpdateOrderCommandDTO(

    @field:NotNull(message = "Customer ID is required")
    @field:Min(value = 1, message = "Customer ID must be positive")
    @field:Schema(description = "ID of the customer placing the order", example = "1")
    val customerId: Long?,

    @field:DecimalMin(value = "0.01", message = "Total amount must be positive")
    @field:Schema(description = "Total amount of the order", example = "99.99")
    val totalAmount: BigDecimal?,

    @field:Schema(description = "Status of the order", example = "COMPLETED")
    val status: Status?,
)


/**
 * DTO for order response
 */
@Schema(description = "Order response payload")
data class OrderResponseDTO(

    @field:Schema(description = "Unique order identifier", example = "507f1f77bcf86cd799439011")
    val id: String,

    @field:Schema(description = "ID of the customer who placed the order", example = "1")
    val customerId: Long,

    @field:Schema(description = "Total amount of the order", example = "99.99")
    val totalAmount: BigDecimal,

    @field:Schema(description = "Current status of the order", example = "PENDING")
    val status: Status,

    @field:Schema(description = "Timestamp when the order was created", example = "2023-09-15T12:34:56Z")
    val createdAt: Instant,
)
