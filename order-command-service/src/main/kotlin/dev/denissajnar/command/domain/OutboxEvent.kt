package dev.denissajnar.command.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * Outbox event document for MongoDB
 * Stores events that need to be published to ensure transactional guarantees
 */
@Document(collection = "outbox_events")
data class OutboxEvent(
    @Id
    val id: String? = null,
    val eventId: String,
    val eventType: String,
    val eventPayload: String,
    val routingKey: String,
    val exchange: String,
    val processed: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val processedAt: Instant? = null,
    val errorMessage: String? = null,
)
