package dev.denissajnar.command.service

import dev.denissajnar.command.dto.CreateOrderDTO
import dev.denissajnar.command.dto.OrderResponseDTO
import dev.denissajnar.command.mapper.toCommand
import dev.denissajnar.command.mapper.toEvent
import dev.denissajnar.command.mapper.toResponseDTO
import dev.denissajnar.command.messaging.EventPublisher
import dev.denissajnar.command.repository.OrderCommandRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * Service for handling order commands in CQRS architecture
 * Responsible for business logic, validation, persistence, and event publishing
 */
@Service
@Transactional
class OrderCommandService(
    private val orderRepository: OrderCommandRepository,
    private val eventPublisher: EventPublisher
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }


    /**
     * Creates a new order based on the provided data transfer object (DTO).
     * Performs validation, persists the order, and publishes an order creation event.
     *
     * @param dto the data transfer object containing the details for creating the order. It must include:
     * - A positive customer ID.
     * - A positive total amount.
     * - A total amount with no more than 2 decimal places.
     * @return an OrderResponseDTO containing the details of the newly created order, including its unique identifier,
     * the customer ID, total amount, order status, and creation timestamp.
     */
    fun createOrder(dto: CreateOrderDTO): OrderResponseDTO =
        dto
            .let {
                validateOrder(it)
                orderRepository.save(it.toCommand())
            }.also {
                eventPublisher.publish(it.toEvent())
                logger.info { "Order created with ID: ${it.id}" }
            }.toResponseDTO()


    /**
     * Validates the provided order creation DTO for required constraints.
     *
     * @param dto the data transfer object containing order creation details. Must include:
     * - A positive customer ID.
     * - A positive total amount.
     * - A total amount with no more than 2 decimal places.
     * Throws an error if any validation constraint is violated.
     */
    private fun validateOrder(dto: CreateOrderDTO) {
        when {
            dto.customerId <= 0 -> error("Customer ID must be positive")
            dto.totalAmount <= BigDecimal.ZERO -> error("Total amount must be positive")
            dto.totalAmount.scale() > 2 -> error("Total amount cannot have more than 2 decimal places")

            else -> logger.debug { "Order validation passed for customer: ${dto.customerId}" }
        }
    }
}