package dev.denissajnar.command.mapper

import dev.denissajnar.command.domain.CommandType
import dev.denissajnar.command.domain.OrderCommand
import dev.denissajnar.command.dto.CreateOrderCommandDTO
import dev.denissajnar.command.dto.OrderResponseDTO
import dev.denissajnar.command.dto.UpdateOrderCommandDTO
import dev.denissajnar.shared.events.OrderCreatedEvent
import dev.denissajnar.shared.events.OrderDeletedEvent
import dev.denissajnar.shared.events.OrderUpdatedEvent
import dev.denissajnar.shared.model.Status
import java.time.Instant

/**
 * Extension function to convert CreateOrderDTO to OrderCommand domain model
 */
fun CreateOrderCommandDTO.toCommand(): OrderCommand = OrderCommand(
    commandType = CommandType.CREATE,
    originalOrderId = null,
    customerId = this.customerId,
    totalAmount = this.totalAmount,
    status = Status.PENDING,
    createdAt = Instant.now(),
    version = 1L,
)

/**
 * Extension function to convert UpdateOrderCommandDTO to OrderCommand domain model
 */
fun UpdateOrderCommandDTO.toCommand(originalOrderCommand: OrderCommand): OrderCommand =
    OrderCommand(
        commandType = CommandType.UPDATE,
        originalOrderId = originalOrderCommand.id,
        customerId = this.customerId ?: originalOrderCommand.customerId,
        totalAmount = this.totalAmount ?: originalOrderCommand.totalAmount,
        status = this.status ?: Status.PENDING,
        version = originalOrderCommand.version + 1L,
    )

/**
 * Extension function to convert OrderCommand to OrderCommand with DELETE command type
 */
fun OrderCommand.toDeleteCommand(): OrderCommand =
    OrderCommand(
        commandType = CommandType.DELETE,
        originalOrderId = this.id,
        customerId = this.customerId,
        totalAmount = this.totalAmount,
        status = this.status,
        version = this.version + 1L,
    )


/**
 * Extension function to convert OrderCommand to OrderCreatedEvent
 * Generates event ID if order ID is null
 */
fun OrderCommand.toEvent(): OrderCreatedEvent = OrderCreatedEvent(
    historyId = this.id.toHexString(),
    customerId = this.customerId,
    totalAmount = this.totalAmount,
    status = this.status,
    timestamp = Instant.now(),
)

/**
 * Extension function to convert OrderCommand to OrderUpdatedEvent
 */
fun OrderCommand.toUpdatedEvent(): OrderUpdatedEvent = OrderUpdatedEvent(
    historyId = this.id.toHexString(),
    customerId = this.customerId,
    totalAmount = this.totalAmount,
    status = this.status,
    timestamp = Instant.now(),
)

/**
 * Extension function to convert OrderCommand to OrderDeletedEvent
 */
fun OrderCommand.toDeletedEvent(originalOrderId: String): OrderDeletedEvent = OrderDeletedEvent(
    historyId = this.id.toHexString(),
    orderId = originalOrderId,
    timestamp = Instant.now(),
)

/**
 * Extension function to convert OrderCommand to OrderResponseDTO
 * Throws exception if order ID is null (should not happen after save)
 */
fun OrderCommand.toResponseDTO(): OrderResponseDTO = OrderResponseDTO(
    id = this.id.toHexString(),
    customerId = this.customerId,
    totalAmount = this.totalAmount,
    status = this.status,
    createdAt = this.createdAt,
)
