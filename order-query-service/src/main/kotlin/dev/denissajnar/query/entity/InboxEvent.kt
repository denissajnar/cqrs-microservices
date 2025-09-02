package dev.denissajnar.query.entity

import jakarta.persistence.*
import java.time.Instant

/**
 * Inbox event entity for idempotent message processing with support for deferred processing
 * Tracks processed events to prevent duplicate processing and handles out-of-order event arrival
 */
@Entity
@Table(name = "inbox_events")
data class InboxEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "event_id", nullable = false, unique = true)
    val eventId: Long,

    @Column(name = "message_id")
    val messageId: String? = null,

    @Column(name = "event_type", nullable = false, length = 100)
    val eventType: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 20)
    val processingStatus: ProcessingStatus = ProcessingStatus.PROCESSED,

    @Column(name = "target_entity_id", length = 100)
    val targetEntityId: String? = null,

    @Column(name = "depends_on_event_type", length = 100)
    val dependsOnEventType: String? = null,

    @Column(name = "processed_at")
    val processedAt: Instant? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "error_message", length = 500)
    val errorMessage: String? = null,

    @Column(name = "event_payload", columnDefinition = "TEXT")
    val eventPayload: String? = null,
)


