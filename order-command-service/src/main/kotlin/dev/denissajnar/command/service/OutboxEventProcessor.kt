package dev.denissajnar.command.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.denissajnar.command.domain.OutboxEvent
import dev.denissajnar.command.repository.OutboxEventRepository
import dev.denissajnar.shared.events.EventType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Processor for outbox events that publishes unprocessed events to RabbitMQ
 * Runs as a scheduled task to ensure eventual consistency
 */
@Component
class OutboxEventProcessor(
    private val outboxEventRepository: OutboxEventRepository,
    private val rabbitTemplate: RabbitTemplate,
) {

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val MAX_BATCH_SIZE = 50
        private val objectMapper: ObjectMapper =
            jacksonObjectMapper().apply {
                registerModule(JavaTimeModule())
            }
    }

    /**
     * Processes unprocessed events from the outbox
     * Runs every 30 seconds
     */
    @Scheduled(fixedDelay = 30000)
    @Transactional
    fun processOutboxEvents() {
        val unprocessedEvents = outboxEventRepository.findByProcessedFalseOrderByCreatedAtAsc()
            .take(MAX_BATCH_SIZE)

        if (unprocessedEvents.isEmpty()) {
            return
        }

        logger.info { "Processing ${unprocessedEvents.size} unprocessed outbox events" }

        for (event in unprocessedEvents) {
            processEvent(event)
        }
    }


    /**
     * Cleans up old processed events
     * Runs every hour
     */
    @Scheduled(fixedDelay = 3600000)
    @Transactional
    fun cleanupProcessedEvents() {
        val cutoffTime = Instant.now().minus(7, ChronoUnit.DAYS)
        val deletedCount = outboxEventRepository.deleteProcessedEventsBefore(cutoffTime)

        if (deletedCount > 0) {
            logger.info { "Cleaned up $deletedCount old processed outbox events" }
        }
    }


    /**
     * Processes and publishes an outbox event to the appropriate message broker.
     * Marks the event as processed and persists the updated state in the repository.
     *
     * @param outboxEvent The outbox event to be processed and published. Includes metadata such as
     *                    exchange, routing key, event payload, and more.
     */
    private fun processEvent(outboxEvent: OutboxEvent) {
        val domainEvent =
            objectMapper.readValue(outboxEvent.eventPayload, EventType.fromTypeName(outboxEvent.eventType).eventClass)

        rabbitTemplate.convertAndSend(
            outboxEvent.exchange,
            outboxEvent.routingKey,
            domainEvent,
        )

        val processedEvent = outboxEvent.copy(
            processed = true,
            processedAt = Instant.now(),
        )
        outboxEventRepository.save(processedEvent)

        logger.info { "Successfully published outbox event: ${outboxEvent.eventType} with ID: ${outboxEvent.eventId}" }
    }
}
