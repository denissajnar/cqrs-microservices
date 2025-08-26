package dev.denissajnar.command.messaging

import dev.denissajnar.shared.events.DomainEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Event publisher for domain events using RabbitMQ
 * Publishes events from command side to be consumed by query side
 */
@Component
class EventPublisher(
    private val rabbitTemplate: RabbitTemplate,
    @param:Value("\${app.messaging.exchange:orders.exchange}")
    private val exchange: String,
    @param:Value("\${app.messaging.routing-key:order.created}")
    private val routingKey: String
) {

    private val logger = LoggerFactory.getLogger(EventPublisher::class.java)

    /**
     * Publishes a domain event to RabbitMQ
     * @param event the domain event to publish
     */
    fun publish(event: DomainEvent) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event)
            logger.info("Published event: ${event::class.simpleName} with ID: ${event.eventId}")
        } catch (exception: Exception) {
            logger.error("Failed to publish event: ${event::class.simpleName} with ID: ${event.eventId}", exception)
            throw exception
        }
    }
}