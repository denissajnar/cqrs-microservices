package dev.denissajnar.query.service

import dev.denissajnar.query.dto.InboxStatsResponse
import dev.denissajnar.query.entity.InboxEvent
import dev.denissajnar.query.entity.ProcessingStatus
import dev.denissajnar.query.repository.InboxEventRepository
import org.springframework.stereotype.Service

/**
 * Service for handling inbox operations
 * Contains business logic for inbox event management and monitoring
 */
@Service
class InboxService(
    private val inboxEventRepository: InboxEventRepository,
) {

    /**
     * Gets all events with a specific processing status
     */
    fun getEventsByStatus(status: ProcessingStatus): List<InboxEvent> =
        inboxEventRepository.findByProcessingStatusOrderByCreatedAtAsc(status)

    /**
     * Gets all failed events in the inbox
     */
    fun getFailedEvents(): List<InboxEvent> =
        inboxEventRepository.findFailedEventsOrderByCreatedAtAsc()

    /**
     * Gets all deferred and failed events (non-processed events)
     */
    fun getPendingEvents(): List<InboxEvent> {
        val pendingStatuses = listOf(
            ProcessingStatus.DEFERRED,
            ProcessingStatus.FAILED,
            ProcessingStatus.EXPIRED,
        )
        return inboxEventRepository.findByProcessingStatusInOrderByCreatedAtAsc(pendingStatuses)
    }

    /**
     * Gets inbox statistics for monitoring
     */
    fun getInboxStats(): InboxStatsResponse {
        val totalEvents = inboxEventRepository.count()
        val processedCount = inboxEventRepository.countByProcessingStatus(ProcessingStatus.PROCESSED)
        val failedCount = inboxEventRepository.countByProcessingStatus(ProcessingStatus.FAILED)
        val deferredCount = inboxEventRepository.countByProcessingStatus(ProcessingStatus.DEFERRED)
        val expiredCount = inboxEventRepository.countByProcessingStatus(ProcessingStatus.EXPIRED)

        return InboxStatsResponse(
            totalEvents = totalEvents,
            processedEvents = processedCount,
            failedEvents = failedCount,
            deferredEvents = deferredCount,
            expiredEvents = expiredCount,
            pendingEvents = (failedCount + deferredCount + expiredCount),
            successRate = if (totalEvents > 0)
                (processedCount.toDouble() / totalEvents * 100).toString() + "%"
            else "N/A",
        )
    }

    /**
     * Gets all events in the inbox with sorting
     */
    fun getAllEvents(): List<InboxEvent> =
        inboxEventRepository.findAll().sortedBy { it.createdAt }

    /**
     * Gets events by event type with filtering and sorting
     */
    fun getEventsByType(eventType: String): List<InboxEvent> =
        inboxEventRepository.findAll()
            .filter { it.eventType.equals(eventType, ignoreCase = true) }
            .sortedBy { it.createdAt }
}
