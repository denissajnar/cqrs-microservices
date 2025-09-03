package dev.denissajnar.command.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.denissajnar.command.domain.OutboxEvent
import dev.denissajnar.command.repository.OutboxEventRepository
import dev.denissajnar.shared.events.DomainEvent
import dev.denissajnar.shared.events.EventType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Event publisher for domain events using the transactional outbox pattern
 * Stores events in the outbox for reliable publishing
 */
@Component
class EventPublisher(
    private val outboxEventRepository: OutboxEventRepository,

    @param:Value($$"${app.messaging.exchange:orders.exchange}")
    private val exchange: String,

    @param:Value($$"${app.messaging.routing-key:order.created}")
    private val routingKey: String,
) {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val objectMapper: ObjectMapper =
            jacksonObjectMapper().apply {
                registerModule(JavaTimeModule())
            }
    }

    /**
     * Publishes a domain event using the transactional outbox pattern
     * The event is stored in the outbox and will be published asynchronously
     * @param event the domain event to publish
     */
    @Transactional
    fun publish(event: DomainEvent) {
        if (outboxEventRepository.existsByEventId(event.eventId)) {
            logger.debug { "Event already exists in outbox: ${event::class.simpleName} with ID: ${event.eventId}" }
            return
        }

        val eventPayload = objectMapper.writeValueAsString(event)
        val outboxEvent = OutboxEvent(
            eventId = event.eventId,
            eventType = EventType.fromEvent(event).typeName,
            eventPayload = eventPayload,
            routingKey = routingKey,
            exchange = exchange,
        )

        outboxEventRepository.save(outboxEvent)

        logger.info { "Event stored in outbox: ${event::class.simpleName} with ID: ${event.eventId}" }
    }
}
