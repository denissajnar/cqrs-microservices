package dev.denissajnar.command.dto.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 * DTO for aggregate version response
 */
@Schema(description = "Aggregate version response payload")
data class AggregateVersionResponse(

    @field:Schema(description = "Current version number of the aggregate for optimistic locking", example = "5")
    val version: Long?,
)
