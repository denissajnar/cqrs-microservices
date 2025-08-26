package dev.denissajnar.query.messaging

import dev.denissajnar.query.mapper.toEntity
import dev.denissajnar.query.repository.OrderRepository
import dev.denissajnar.shared.events.OrderCreatedEvent
import org.slf4j.LoggerFactory
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
    private val orderRepository: OrderRepository
) {

    private val logger = LoggerFactory.getLogger(EventHandler::class.java)

    /**
     * Handles OrderCreatedEvent by creating the corresponding read model
     * @param event the order created event from the command side
     */
    @RabbitListener(queues = ["\${app.messaging.queue:orders.query.queue}"])
    fun handleOrderCreatedEvent(event: OrderCreatedEvent) {
        try {
            logger.info("Processing OrderCreatedEvent for order: ${event.orderId}")

            // Convert event to entity using extension function and save
            val orderEntity = event.toEntity()
            orderRepository.save(orderEntity)

            logger.info("Successfully processed OrderCreatedEvent for order: ${event.orderId}")
        } catch (exception: Exception) {
            logger.error("Failed to process OrderCreatedEvent for order: ${event.orderId}", exception)
            // TODO
            // - Send to dead letter queue
            // - Implement retry logic
            throw exception // Re-throw to trigger retry mechanism if configured
        }
    }
}