package dev.denissajnar.command.controller

import dev.denissajnar.command.domain.OutboxEvent
import dev.denissajnar.command.dto.OutboxStatsDTO
import dev.denissajnar.command.service.OutboxService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for outbox monitoring operations
 * Provides visibility into the outbox pattern implementation
 */
@Validated
@RestController
@RequestMapping("/api/v1/outbox")
@Tag(name = "Outbox Monitoring", description = "Operations for monitoring outbox events")
class OutboxController(
    private val outboxService: OutboxService,
) {

    /**
     * Gets all unprocessed events in the outbox
     */
    @GetMapping("/unprocessed")
    @Operation(
        summary = "Get unprocessed events",
        description = "Retrieves all events that have not been published yet",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Unprocessed events retrieved successfully"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun getUnprocessedEvents(): ResponseEntity<List<OutboxEvent>> =
        ResponseEntity.ok(outboxService.getUnprocessedEvents())

    /**
     * Gets outbox statistics for monitoring
     */
    @GetMapping("/stats")
    @Operation(
        summary = "Get outbox statistics",
        description = "Retrieves statistics about outbox event processing",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun getOutboxStats(): ResponseEntity<OutboxStatsDTO> =
        ResponseEntity.ok(outboxService.getOutboxStats())


    /**
     * Gets all events in the outbox with pagination
     */
    @GetMapping
    @Operation(
        summary = "Get all outbox events",
        description = "Retrieves all outbox events for debugging purposes",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun getAllEvents(): ResponseEntity<List<OutboxEvent>> =
        ResponseEntity.ok(outboxService.getAllEvents())
}
