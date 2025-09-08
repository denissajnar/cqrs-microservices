package dev.denissajnar.command.dto.request

import dev.denissajnar.shared.model.Status
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

/**
 * DTO for updating an existing order
 */
@Schema(description = "Request payload for updating an existing order")
data class UpdateOrderCommandRequest(

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
