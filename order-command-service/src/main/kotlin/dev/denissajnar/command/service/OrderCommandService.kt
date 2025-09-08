package dev.denissajnar.command.service

import dev.denissajnar.command.dto.request.CreateOrderCommandRequest
import dev.denissajnar.command.dto.request.UpdateOrderCommandRequest
import dev.denissajnar.command.dto.response.OrderResponse
import dev.denissajnar.command.exception.BusinessValidationException
import dev.denissajnar.command.mapper.toDeleteEntity
import dev.denissajnar.command.mapper.toEntity
import dev.denissajnar.command.mapper.toEvent
import dev.denissajnar.command.mapper.toResponse
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
    fun createOrder(request: CreateOrderCommandRequest): OrderResponse =
        request
            .let {
                orderValidator.validateForCreation(it)
                orderCommandRepository.save(it.toEntity())
            }.also {
                eventPublisher.publish(it.toEvent())
                logger.info { "Order created with ID: ${it.id}" }
            }.toResponse()

    /**
     * Updates an existing order by creating a new command entry (event sourcing)
     * Does NOT modify existing MongoDB document - creates new historical record
     */
    fun updateOrder(id: String, request: UpdateOrderCommandRequest): OrderResponse =
        orderCommandRepository.findByIdOrNull(ObjectId(id))
            ?.also {
                orderValidator.validateForUpdate(request, it)
            }?.let { existingOrder ->
                val updateCommand = request.toEntity(existingOrder)
                orderCommandRepository.save(updateCommand)
            }?.also { updateCommand ->
                eventPublisher.publish(updateCommand.toEvent())
                logger.info { "Order update command created with ID: ${updateCommand.id}" }
            }?.toResponse() ?: throw BusinessValidationException("Order not found: $id")

    /**
     * Deletes an order by creating a new command entry (event sourcing)
     * Does NOT modify existing MongoDB document - creates new historical record
     */
    fun deleteOrder(id: String) =
        orderCommandRepository.findByIdOrNull(ObjectId(id))
            ?.let { existingOrder ->
                orderValidator.validateForDeletion(existingOrder)
                val deleteCommand = existingOrder.toDeleteEntity()
                orderCommandRepository.save(deleteCommand)
            }?.also { deleteCommand ->
                eventPublisher.publish(deleteCommand.toEvent())
                logger.info { "Order deletion command created with ID: ${deleteCommand.id}" }
            } ?: throw BusinessValidationException("Order not found: $id")
}
