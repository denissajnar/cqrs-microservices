package dev.denissajnar.command.controller

import dev.denissajnar.command.domain.OrderAggregate
import dev.denissajnar.command.domain.OrderCommand
import dev.denissajnar.command.service.EventSourcingService
import dev.denissajnar.shared.model.AggregateStats
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.bson.types.ObjectId
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

/**
 * REST controller for event sourcing operations
 * Provides debugging and monitoring capabilities for event-sourced aggregates
 */
@Validated
@RestController
@RequestMapping("/api/v1/event-sourcing")
@Tag(name = "Event Sourcing", description = "Event sourcing operations for debugging and monitoring")
class EventSourcingController(
    private val eventSourcingService: EventSourcingService,
) {

    /**
     * Reconstructs an aggregate from its event history
     */
    @GetMapping("/aggregates/{aggregateId}/reconstruct")
    @Operation(
        summary = "Reconstruct aggregate from events",
        description = "Rebuilds aggregate state from its complete event history",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Aggregate reconstructed successfully"),
            ApiResponse(responseCode = "404", description = "Aggregate not found"),
            ApiResponse(responseCode = "400", description = "Invalid aggregate ID"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun reconstructAggregate(
        @Parameter(description = "Aggregate ID", example = "550e8400e29b41d4a716446655440000")
        @PathVariable aggregateId: String,
    ): ResponseEntity<OrderAggregate> {
        return try {
            val objectId = ObjectId(aggregateId)
            eventSourcingService.reconstructAggregate(objectId)
                ?.let { ResponseEntity.ok(it) }
                ?: ResponseEntity.notFound().build()
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * Gets the complete event history for an aggregate
     */
    @GetMapping("/aggregates/{aggregateId}/events")
    @Operation(
        summary = "Get event history",
        description = "Retrieves all events for an aggregate in chronological order",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Event history retrieved successfully"),
            ApiResponse(responseCode = "400", description = "Invalid aggregate ID"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun getEventHistory(
        @Parameter(description = "Aggregate ID", example = "550e8400e29b41d4a716446655440000")
        @PathVariable aggregateId: String,
    ): ResponseEntity<List<OrderCommand>> {
        return try {
            val objectId = ObjectId(aggregateId)
            val events = eventSourcingService.getEventHistory(objectId)
            ResponseEntity.ok(events)
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * Gets aggregate statistics for monitoring
     */
    @GetMapping("/aggregates/{aggregateId}/stats")
    @Operation(
        summary = "Get aggregate statistics",
        description = "Retrieves metadata and statistics about an aggregate",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            ApiResponse(responseCode = "404", description = "Aggregate not found"),
            ApiResponse(responseCode = "400", description = "Invalid aggregate ID"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun getAggregateStats(
        @Parameter(description = "Aggregate ID", example = "550e8400e29b41d4a716446655440000")
        @PathVariable aggregateId: String,
    ): ResponseEntity<AggregateStats> {
        return try {
            val objectId = ObjectId(aggregateId)
            eventSourcingService.getAggregateStats(objectId)
                ?.let { ResponseEntity.ok(it) }
                ?: ResponseEntity.notFound().build()
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * Replays events for debugging purposes
     */
    @PostMapping("/aggregates/{aggregateId}/replay")
    @Operation(
        summary = "Replay events",
        description = "Replays all events for an aggregate with detailed logging for debugging",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Events replayed successfully"),
            ApiResponse(responseCode = "404", description = "Aggregate not found"),
            ApiResponse(responseCode = "400", description = "Invalid aggregate ID"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun replayEvents(
        @Parameter(description = "Aggregate ID", example = "550e8400e29b41d4a716446655440000")
        @PathVariable aggregateId: String,
    ): ResponseEntity<OrderAggregate> {
        return try {
            val objectId = ObjectId(aggregateId)
            eventSourcingService.replayEvents(objectId)
                ?.let { ResponseEntity.ok(it) }
                ?: ResponseEntity.notFound().build()
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * Validates that an aggregate exists and is not deleted
     */
    @GetMapping("/aggregates/{aggregateId}/validate")
    @Operation(
        summary = "Validate aggregate existence",
        description = "Checks if an aggregate exists and is not in deleted state",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Validation completed"),
            ApiResponse(responseCode = "400", description = "Invalid aggregate ID"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun validateAggregateExists(
        @Parameter(description = "Aggregate ID", example = "550e8400e29b41d4a716446655440000")
        @PathVariable aggregateId: String,
    ): ResponseEntity<Map<String, Boolean>> {
        return try {
            val objectId = ObjectId(aggregateId)
            val exists = eventSourcingService.validateAggregateExists(objectId)
            ResponseEntity.ok(mapOf("exists" to exists))
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * Gets the current state of an aggregate
     */
    @GetMapping("/aggregates/{aggregateId}/current-state")
    @Operation(
        summary = "Get current aggregate state",
        description = "Retrieves the current state of an aggregate by reconstructing from events",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Current state retrieved successfully"),
            ApiResponse(responseCode = "404", description = "Aggregate not found"),
            ApiResponse(responseCode = "400", description = "Invalid aggregate ID"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun getCurrentAggregateState(
        @Parameter(description = "Aggregate ID", example = "550e8400e29b41d4a716446655440000")
        @PathVariable aggregateId: String,
    ): ResponseEntity<OrderAggregate> {
        return try {
            val objectId = ObjectId(aggregateId)
            val currentState = eventSourcingService.getCurrentAggregateState(objectId)
            ResponseEntity.ok(currentState)
        } catch (_: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (_: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Gets the latest version of an aggregate
     */
    @GetMapping("/aggregates/{aggregateId}/version")
    @Operation(
        summary = "Get aggregate version",
        description = "Retrieves the current version number of an aggregate for optimistic locking",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Version retrieved successfully"),
            ApiResponse(responseCode = "404", description = "Aggregate not found"),
            ApiResponse(responseCode = "400", description = "Invalid aggregate ID"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun getLatestVersion(
        @Parameter(description = "Aggregate ID", example = "550e8400e29b41d4a716446655440000")
        @PathVariable aggregateId: String,
    ): ResponseEntity<Map<String, Long?>> {
        return try {
            val objectId = ObjectId(aggregateId)
            val version = eventSourcingService.getLatestVersion(objectId)
            if (version != null) {
                ResponseEntity.ok(mapOf("version" to version))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }
}
