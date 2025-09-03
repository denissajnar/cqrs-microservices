package dev.denissajnar.query.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.denissajnar.query.entity.InboxEvent
import dev.denissajnar.query.entity.ProcessingStatus
import dev.denissajnar.query.repository.InboxEventRepository
import dev.denissajnar.shared.events.DomainEvent
import dev.denissajnar.shared.events.EventType
import dev.denissajnar.shared.events.OrderEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets

/**
 * Event handler for storing domain events from the command side using the inbox pattern
 * Stores events for deferred processing by InboxEventProcessor with idempotency guarantees
 */
@Component
@Transactional
class EventHandler(
    private val inboxEventRepository: InboxEventRepository,
) {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val objectMapper: ObjectMapper =
            jacksonObjectMapper().apply {
                registerModule(JavaTimeModule())
            }
    }

    /**
     * Stores an event in the inbox for deferred processing
     * @param event the domain event to store
     * @param eventType the type name of the event
     * @param eventPayload the serialized event payload
     */
    private fun <T> storeEventInInbox(
        event: T,
        eventType: String,
        eventPayload: String,
    ) where T : DomainEvent {
        try {
            val eventIdLong = convertStringToLong(event.eventId)

            if (inboxEventRepository.existsByEventId(eventIdLong)) {
                logger.debug { "Event already exists in inbox, skipping: $eventType with ID: ${event.eventId}" }
                return
            }

            val inboxEvent = InboxEvent(
                eventId = eventIdLong,
                messageId = event.messageId,
                eventType = eventType,
                processingStatus = ProcessingStatus.DEFERRED,
                eventPayload = eventPayload,
            )
            inboxEventRepository.save(inboxEvent)

            logger.info { "Successfully stored in inbox for deferred processing: $eventType with ID: ${event.eventId}" }

        } catch (exception: Exception) {
            logger.error(exception) { "Failed to store $eventType with ID: ${event.eventId} in inbox" }
            throw exception
        }
    }

    /**
     * Converts String eventId to Long for database storage
     * Uses consistent hash function to ensure same String always produces same Long
     */
    private fun convertStringToLong(eventId: String): Long = eventId.hashCode().toLong()

    /**
     * Handles unified OrderEvent for all order operations (create, update, delete)
     * Uses inbox pattern for idempotent processing
     * @param message the raw RabbitMQ message
     */
    @RabbitListener(
        queues = [$$"${app.messaging.queue:orders.query.queue}"],
    )
    fun handleOrderEvent(message: Message) {
        val messageBody = String(message.body, StandardCharsets.UTF_8)
        logger.debug { "Received OrderEvent message: $messageBody" }

        val event: OrderEvent = objectMapper.readValue(messageBody)

        storeEventInInbox(event, EventType.fromEvent(event).typeName, messageBody)
    }
}
