package dev.denissajnar.query.messaging

import dev.denissajnar.query.mapper.toEntity
import dev.denissajnar.query.repository.OrderQueryRepository
import dev.denissajnar.shared.events.OrderCreatedEvent
import dev.denissajnar.shared.events.OrderDeletedEvent
import dev.denissajnar.shared.events.OrderUpdatedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Event handler for processing domain events from the command side
 * Updates the query side database when events are received
 */
@Component
@Transactional
class EventHandler(
    private val orderQueryRepository: OrderQueryRepository,
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Handles OrderCreatedEvent by creating the corresponding read model
     * @param event the order created event from the command side
     */
    @RabbitListener(queues = [$$"${app.messaging.queue:orders.query.queue}"])
    fun handleOrderCreatedEvent(event: OrderCreatedEvent) {
        try {
            logger.info { "Processing OrderCreatedEvent for order: ${event.historyId}" }

            // Convert event to entity using extension function and save
            val orderEntity = event.toEntity()
            orderQueryRepository.save(orderEntity)

            logger.info { "Successfully processed OrderCreatedEvent for order: ${event.historyId}" }
        } catch (exception: Exception) {
            logger.error { "Failed to process OrderCreatedEvent for order: ${event.historyId}" }
            throw exception // Re-throw to trigger retry mechanism if configured
        }
    }

    /**
     * Handles OrderUpdatedEvent by updating the corresponding read model
     * @param event the order updated event from the command side
     */
    @RabbitListener(queues = [$$"${app.messaging.queue:orders.query.queue}"])
    fun handleOrderUpdatedEvent(event: OrderUpdatedEvent) {
        try {
            logger.info { "Processing OrderUpdatedEvent for order: ${event.historyId}" }

            // Find existing order by historyId and update it, or create new if not found
            val existingOrder = orderQueryRepository.findByHistoryId(event.historyId)
            if (existingOrder != null) {
                // Update existing order
                existingOrder.customerId = event.customerId
                existingOrder.totalAmount = event.totalAmount
                existingOrder.status = event.status
                orderQueryRepository.save(existingOrder)
                logger.info { "Updated existing order with historyId: ${event.historyId}" }
            } else {
                logger.error { "Order not found for historyId: ${event.historyId}" }
            }

            logger.info { "Successfully processed OrderUpdatedEvent for order: ${event.historyId}" }
        } catch (exception: Exception) {
            logger.error { "Failed to process OrderUpdatedEvent for order: ${event.historyId}" }
            throw exception // Re-throw to trigger retry mechanism if configured
        }
    }

    /**
     * Handles OrderDeletedEvent by removing the corresponding read model
     * @param event the order deleted event from the command side
     */
    @RabbitListener(queues = [$$"${app.messaging.queue:orders.query.queue}"])
    fun handleOrderDeletedEvent(event: OrderDeletedEvent) {
        try {
            logger.info { "Processing OrderDeletedEvent for order: ${event.historyId}" }

            // Find and delete the order by historyId
            val existingOrder = orderQueryRepository.findByHistoryId(event.historyId)
            if (existingOrder != null) {
                orderQueryRepository.delete(existingOrder)
                logger.info { "Successfully deleted order with historyId: ${event.historyId}" }
            } else {
                logger.warn { "Order not found for deletion with historyId: ${event.historyId}" }
            }

            logger.info { "Successfully processed OrderDeletedEvent for order: ${event.historyId}" }
        } catch (exception: Exception) {
            logger.error { "Failed to process OrderDeletedEvent for order: ${event.historyId}" }
            throw exception // Re-throw to trigger retry mechanism if configured
        }
    }
}
