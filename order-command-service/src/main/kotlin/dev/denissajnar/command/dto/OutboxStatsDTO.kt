package dev.denissajnar.command.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * DTO for outbox statistics
 */
@Schema(description = "Outbox statistics response payload")
data class OutboxStatsDTO(

    @field:Schema(description = "Total number of events in the outbox", example = "100")
    val totalEvents: Int,

    @field:Schema(description = "Number of successfully processed events", example = "85")
    val processedEvents: Int,

    @field:Schema(description = "Number of unprocessed events", example = "15")
    val unprocessedEvents: Int,

    @field:Schema(description = "Processing rate as a percentage", example = "85%")
    val processingRate: String,
)
