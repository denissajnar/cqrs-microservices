package dev.denissajnar.query.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.denissajnar.query.entity.InboxEvent
import dev.denissajnar.query.entity.OrderQuery
import dev.denissajnar.query.entity.ProcessingStatus
import dev.denissajnar.query.repository.InboxEventRepository
import dev.denissajnar.query.repository.OrderQueryRepository
import dev.denissajnar.shared.events.DomainEvent
import dev.denissajnar.shared.events.OrderEvent
import dev.denissajnar.shared.events.OrderOperationType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Processor for inbox events that processes unprocessed events using scheduled jobs
 * Provides idempotent processing with proper error handling and retry logic
 */
@Component
class InboxEventProcessor(
    private val inboxEventRepository: InboxEventRepository,
    private val orderQueryRepository: OrderQueryRepository,
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
     * Processes unprocessed events from the inbox
     * Runs every 30 seconds
     */
    @Scheduled(fixedDelay = 30000)
    @Transactional
    fun processInboxEvents() {
        val unprocessedEvents = inboxEventRepository
            .findByProcessingStatusInOrderByCreatedAtAsc(
                listOf(ProcessingStatus.DEFERRED, ProcessingStatus.FAILED),
            )
            .take(MAX_BATCH_SIZE)

        if (unprocessedEvents.isEmpty()) {
            return
        }

        logger.info { "Processing ${unprocessedEvents.size} unprocessed inbox events" }

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
        val deletedCount = inboxEventRepository.deleteProcessedEventsBefore(cutoffTime)

        if (deletedCount > 0) {
            logger.info { "Cleaned up $deletedCount old processed inbox events" }
        }
    }

    /**
     * Maps event type string to the unified OrderEvent class
     */
    private fun getEventClass(eventType: String): Class<out DomainEvent> =
        when (eventType) {
            "OrderCreatedEvent", "OrderUpdatedEvent", "OrderDeletedEvent" -> OrderEvent::class.java
            else -> throw IllegalArgumentException("Unknown event type: $eventType")
        }

    private fun processEvent(inboxEvent: InboxEvent) {
        if (inboxEvent.eventPayload.isNullOrBlank()) {
            logger.error { "Event payload is missing for event: ${inboxEvent.eventType} with ID: ${inboxEvent.eventId}" }
            markEventAsFailed(inboxEvent, "Event payload is missing")
            return
        }

        val eventClass = getEventClass(inboxEvent.eventType)
        val domainEvent = objectMapper.readValue(inboxEvent.eventPayload, eventClass) as OrderEvent

        processOrderEvent(domainEvent)

        val processedEvent = inboxEvent.copy(
            processingStatus = ProcessingStatus.PROCESSED,
            processedAt = Instant.now(),
            errorMessage = null,
        )
        inboxEventRepository.save(processedEvent)

        logger.info { "Successfully processed inbox event: ${inboxEvent.eventType} with ID: ${inboxEvent.eventId}" }
    }

    private fun markEventAsFailed(inboxEvent: InboxEvent, errorMessage: String) {
        val failedEvent = inboxEvent.copy(
            processingStatus = ProcessingStatus.FAILED,
            errorMessage = errorMessage.take(500), // Limit error message length
        )
        inboxEventRepository.save(failedEvent)
    }

    private fun processOrderEvent(event: OrderEvent) {
        logger.info { "Processing OrderEvent (${event.operationType}) for order: ${event.orderId}" }

        when (event.operationType) {
            OrderOperationType.CREATE -> {
                val orderEntity = createOrderEntityFromEvent(event)
                orderQueryRepository.save(orderEntity)
                logger.info { "Successfully created order with orderId: ${event.orderId}" }
            }

            OrderOperationType.UPDATE -> {
                val existingOrder = orderQueryRepository.findByOrderId(event.orderId)
                if (existingOrder != null) {
                    event.customerId?.let { existingOrder.customerId = it }
                    event.totalAmount?.let { existingOrder.totalAmount = it }
                    event.status?.let { existingOrder.status = it }
                    orderQueryRepository.save(existingOrder)
                    logger.info { "Successfully updated order with orderId: ${event.orderId}" }
                } else {
                    logger.error { "Order not found for orderId: ${event.orderId}" }
                    throw IllegalStateException("Order not found for orderId: ${event.orderId}")
                }
            }

            OrderOperationType.DELETE -> {
                val existingOrder = orderQueryRepository.findByOrderId(event.orderId)
                if (existingOrder != null) {
                    orderQueryRepository.delete(existingOrder)
                    logger.info { "Successfully deleted order with orderId: ${event.orderId}" }
                } else {
                    logger.warn { "Order not found for deletion with orderId: ${event.orderId}" }
                }
            }
        }

        logger.info { "Successfully processed OrderEvent (${event.operationType}) for order: ${event.orderId}" }
    }

    private fun createOrderEntityFromEvent(event: OrderEvent) =
        OrderQuery(
            orderId = event.orderId,
            customerId = requireNotNull(event.customerId) { "customerId is required for CREATE operation" },
            totalAmount = requireNotNull(event.totalAmount) { "totalAmount is required for CREATE operation" },
            status = requireNotNull(event.status) { "status is required for CREATE operation" },
        )
}
