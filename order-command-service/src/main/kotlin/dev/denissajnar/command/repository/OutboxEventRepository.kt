package dev.denissajnar.command.repository

import dev.denissajnar.command.domain.OutboxEvent
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * Repository for outbox events in MongoDB
 * Handles persistence operations for the outbox pattern
 */
@Repository
interface OutboxEventRepository : MongoRepository<OutboxEvent, String> {

    /**
     * Find all unprocessed events ordered by creation time
     */
    fun findByProcessedFalseOrderByCreatedAtAsc(): List<OutboxEvent>

    /**
     * Check if an event with the given ID exists
     */
    fun existsByEventId(eventId: String): Boolean

    /**
     * Delete old processed events for cleanup
     */
    @Query(value = $$"{ 'processed': true, 'processedAt': { '$lt': ?0 } }", delete = true)
    fun deleteProcessedEventsBefore(cutoffTime: Instant): Long
}
