package dev.denissajnar.command.mapper

import dev.denissajnar.command.domain.CommandType
import dev.denissajnar.command.domain.OrderCommand
import dev.denissajnar.command.dto.CreateOrderCommandDTO
import dev.denissajnar.command.dto.OrderResponseDTO
import dev.denissajnar.command.dto.UpdateOrderCommandDTO
import dev.denissajnar.shared.events.OrderEvent
import dev.denissajnar.shared.events.OrderOperationType
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
 * Extension function to convert OrderCommand to unified OrderEvent
 * Uses the command ID as messageId and determines operationType from commandType
 */
fun OrderCommand.toOrderEvent(): OrderEvent {
    val operationType = when (this.commandType) {
        CommandType.CREATE -> OrderOperationType.CREATE
        CommandType.UPDATE -> OrderOperationType.UPDATE
        CommandType.DELETE -> OrderOperationType.DELETE
    }

    return OrderEvent(
        messageId = this.id.toHexString(),
        operationType = operationType,
        orderId = when (this.commandType) {
            CommandType.CREATE -> this.id.toHexString()
            CommandType.UPDATE -> this.originalOrderId?.toHexString() ?: this.id.toHexString()
            CommandType.DELETE -> this.originalOrderId?.toHexString() ?: this.id.toHexString()
        },
        customerId = this.customerId,
        totalAmount = this.totalAmount,
        status = this.status,
        timestamp = Instant.now(),
    )
}

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
