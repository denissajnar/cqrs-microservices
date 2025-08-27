package dev.denissajnar.command.service

import dev.denissajnar.command.dto.CreateOrderCommandDTO
import dev.denissajnar.command.dto.OrderResponseDTO
import dev.denissajnar.command.dto.UpdateOrderCommandDTO
import dev.denissajnar.command.exception.BusinessValidationException
import dev.denissajnar.command.mapper.*
import dev.denissajnar.command.messaging.EventPublisher
import dev.denissajnar.command.repository.OrderCommandRepository
import dev.denissajnar.command.validation.OrderValidator
import io.github.oshai.kotlinlogging.KotlinLogging
import org.bson.types.ObjectId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service for handling order commands in CQRS architecture
 * Responsible for business logic, validation, persistence, and event publishing
 */
@Service
@Transactional
class OrderCommandService(
    private val orderCommandRepository: OrderCommandRepository,
    private val eventPublisher: EventPublisher,
    private val orderValidator: OrderValidator,
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Creates a new order based on the provided data transfer object (DTO).
     * Performs validation, persists the order, and publishes an order creation event.
     */
    fun createOrder(dto: CreateOrderCommandDTO): OrderResponseDTO =
        dto
            .let {
                orderValidator.validateForCreation(it)
                orderCommandRepository.save(it.toCommand())
            }.also {
                eventPublisher.publish(it.toEvent())
                logger.info { "Order created with ID: ${it.id}" }
            }.toResponseDTO()

    /**
     * Updates an order by creating a new command entry (event sourcing)
     * Does NOT modify existing MongoDB document - creates new historical record
     */
    fun updateOrder(id: String, dto: UpdateOrderCommandDTO): OrderResponseDTO =
        dto
            .let {
                orderCommandRepository.findByIdOrNull(ObjectId(id))
            }?.also {
                orderValidator.validateForUpdate(dto, it)
            }?.let {
                dto.toCommand(it)
            }?.also {
                eventPublisher.publish(it.toUpdatedEvent())
                logger.info { "Order update command created with ID: ${it.id}" }
            }?.toResponseDTO() ?: throw BusinessValidationException("Order not found: $id")

    /**
     * Deletes an order by creating a new command entry (event sourcing)
     * Does NOT modify existing MongoDB document - creates new historical record
     */
    fun deleteOrder(id: String) =
        orderCommandRepository.findByIdOrNull(ObjectId(id))
            ?.let { existingOrder ->
                orderValidator.validateForDeletion(existingOrder)
                val deleteCommand = existingOrder.toDeleteCommand()
                orderCommandRepository.save(deleteCommand)
            }?.also { deleteCommand ->
                eventPublisher.publish(deleteCommand.toDeletedEvent(id))
                logger.info { "Order deletion command created with ID: ${deleteCommand.id}" }
            } ?: throw BusinessValidationException("Order not found: $id")
}
