package dev.denissajnar.command.dto.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 * DTO for aggregate existence validation response
 */
@Schema(description = "Aggregate existence validation response payload")
data class AggregateExistsRsponse(

    @field:Schema(description = "Whether the aggregate exists and is not deleted", example = "true")
    val exists: Boolean,
)
