package dev.denissajnar.command.service

import dev.denissajnar.command.domain.OutboxEvent
import dev.denissajnar.command.dto.response.OutboxStatsResponse
import dev.denissajnar.command.repository.OutboxEventRepository
import org.springframework.stereotype.Service

/**
 * Service for handling outbox operations
 * Contains business logic for outbox event management and monitoring
 */
@Service
class OutboxService(
    private val outboxEventRepository: OutboxEventRepository,
) {

    /**
     * Gets all unprocessed events in the outbox
     */
    fun getUnprocessedEvents(): List<OutboxEvent> = outboxEventRepository.findByProcessedFalseOrderByCreatedAtAsc()

    /**
     * Gets outbox statistics for monitoring
     */
    fun getOutboxStats(): OutboxStatsResponse {
        val allEvents = outboxEventRepository.findAll().toList()
        val unprocessedCount = outboxEventRepository.findByProcessedFalseOrderByCreatedAtAsc().size
        val processedCount = allEvents.size - unprocessedCount

        return OutboxStatsResponse(
            totalEvents = allEvents.size,
            processedEvents = processedCount,
            unprocessedEvents = unprocessedCount,
            processingRate = if (allEvents.isNotEmpty())
                (processedCount.toDouble() / allEvents.size * 100).toString() + "%"
            else "N/A",
        )
    }

    /**
     * Gets all events in the outbox with sorting
     */
    fun getAllEvents(): List<OutboxEvent> = outboxEventRepository.findAll().sortedBy { it.createdAt }
}
