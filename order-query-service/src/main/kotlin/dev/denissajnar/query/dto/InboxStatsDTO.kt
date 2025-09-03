package dev.denissajnar.query.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * DTO for inbox statistics
 */
@Schema(description = "Inbox statistics response payload")
data class InboxStatsDTO(

    @field:Schema(description = "Total number of events in the inbox", example = "100")
    val totalEvents: Long,

    @field:Schema(description = "Number of successfully processed events", example = "85")
    val processedEvents: Long,

    @field:Schema(description = "Number of failed events", example = "5")
    val failedEvents: Long,

    @field:Schema(description = "Number of deferred events", example = "8")
    val deferredEvents: Long,

    @field:Schema(description = "Number of expired events", example = "2")
    val expiredEvents: Long,

    @field:Schema(description = "Number of pending events (failed + deferred + expired)", example = "15")
    val pendingEvents: Long,

    @field:Schema(description = "Success rate as a percentage", example = "85%")
    val successRate: String,
)
