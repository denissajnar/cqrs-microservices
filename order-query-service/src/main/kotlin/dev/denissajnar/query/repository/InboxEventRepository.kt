package dev.denissajnar.query.repository

import dev.denissajnar.query.entity.InboxEvent
import dev.denissajnar.query.entity.ProcessingStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * Repository for inbox events in the query service
 * Handles persistence operations for the inbox pattern
 */
@Repository
interface InboxEventRepository : JpaRepository<InboxEvent, Long> {

    /**
     * Check if an event with the given ID exists
     */
    fun existsByEventId(eventId: Long): Boolean

    /**
     * Find all events by processing status ordered by creation time
     */
    fun findByProcessingStatusOrderByCreatedAtAsc(status: ProcessingStatus): List<InboxEvent>

    /**
     * Find all failed events ordered by creation time
     */
    fun findFailedEventsOrderByCreatedAtAsc(): List<InboxEvent> =
        findByProcessingStatusOrderByCreatedAtAsc(ProcessingStatus.FAILED)

    /**
     * Find all deferred events ordered by creation time
     */
    fun findByProcessingStatusInOrderByCreatedAtAsc(statuses: List<ProcessingStatus>): List<InboxEvent>

    /**
     * Count events by processing status
     */
    fun countByProcessingStatus(status: ProcessingStatus): Long

    /**
     * Delete processed events older than the specified time
     */
    @Modifying
    @Query("DELETE FROM InboxEvent e WHERE e.processingStatus = 'PROCESSED' AND e.processedAt < :cutoffTime")
    fun deleteProcessedEventsBefore(cutoffTime: Instant): Int
}
