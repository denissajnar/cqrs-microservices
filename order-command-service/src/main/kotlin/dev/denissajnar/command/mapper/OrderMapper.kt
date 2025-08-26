package dev.denissajnar.command.mapper

import dev.denissajnar.command.domain.OrderCommand
import dev.denissajnar.command.dto.CreateOrderDTO
import dev.denissajnar.command.dto.OrderResponseDTO
import dev.denissajnar.shared.events.OrderCreatedEvent
import java.time.Instant
import java.util.*

/**
 * Extension function to convert CreateOrderDTO to OrderCommand domain model
 */
fun CreateOrderDTO.toCommand(): OrderCommand = OrderCommand(
    customerId = this.customerId,
    totalAmount = this.totalAmount
)

/**
 * Extension function to convert OrderCommand to OrderCreatedEvent
 * Generates event ID if order ID is null
 */
fun OrderCommand.toEvent(): OrderCreatedEvent = OrderCreatedEvent(
    eventId = UUID.randomUUID(),
    orderId = this.id ?: UUID.randomUUID(),
    customerId = this.customerId,
    totalAmount = this.totalAmount,
    status = this.status,
    timestamp = Instant.now()
)

/**
 * Extension function to convert OrderCommand to OrderResponseDTO
 * Throws exception if order ID is null (should not happen after save)
 */
fun OrderCommand.toResponseDTO(): OrderResponseDTO = OrderResponseDTO(
    id = checkNotNull(this.id) { "Order ID cannot be null in response" },
    customerId = this.customerId,
    totalAmount = this.totalAmount,
    status = this.status,
    createdAt = this.createdAt
)