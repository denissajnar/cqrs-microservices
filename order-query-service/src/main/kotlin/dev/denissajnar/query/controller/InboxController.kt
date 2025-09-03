package dev.denissajnar.query.controller

import dev.denissajnar.query.dto.InboxStatsDTO
import dev.denissajnar.query.entity.InboxEvent
import dev.denissajnar.query.entity.ProcessingStatus
import dev.denissajnar.query.service.InboxService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for inbox monitoring operations
 * Provides visibility into the inbox pattern implementation
 */
@Validated
@RestController
@RequestMapping("/api/v1/inbox")
@Tag(name = "Inbox Monitoring", description = "Operations for monitoring inbox events")
class InboxController(
    private val inboxService: InboxService,
) {

    /**
     * Gets all events with a specific processing status
     */
    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get events by processing status",
        description = "Retrieves all events that match the specified processing status",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
            ApiResponse(responseCode = "400", description = "Invalid processing status"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun getEventsByStatus(
        @Parameter(description = "Processing status", example = "PROCESSED")
        @PathVariable status: ProcessingStatus,
    ): ResponseEntity<List<InboxEvent>> =
        ResponseEntity.ok(inboxService.getEventsByStatus(status))

    /**
     * Gets all failed events in the inbox
     */
    @GetMapping("/failed")
    @Operation(
        summary = "Get failed events",
        description = "Retrieves all events that failed to process",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Failed events retrieved successfully"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun getFailedEvents(): ResponseEntity<List<InboxEvent>> =
        ResponseEntity.ok(inboxService.getFailedEvents())

    /**
     * Gets all deferred and failed events (non-processed events)
     */
    @GetMapping("/pending")
    @Operation(
        summary = "Get pending events",
        description = "Retrieves all events that are deferred, failed, or expired (not processed)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Pending events retrieved successfully"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun getPendingEvents(): ResponseEntity<List<InboxEvent>> =
        ResponseEntity.ok(inboxService.getPendingEvents())


    /**
     * Gets inbox statistics for monitoring
     */
    @GetMapping("/stats")
    @Operation(
        summary = "Get inbox statistics",
        description = "Retrieves statistics about inbox event processing",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun getInboxStats(): ResponseEntity<InboxStatsDTO> =
        ResponseEntity.ok(inboxService.getInboxStats())

    /**
     * Gets all events in the inbox with pagination support
     */
    @GetMapping
    @Operation(
        summary = "Get all inbox events",
        description = "Retrieves all inbox events for debugging purposes",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun getAllEvents(): ResponseEntity<List<InboxEvent>> =
        ResponseEntity.ok(inboxService.getAllEvents())


    /**
     * Gets events by event type
     */
    @GetMapping("/type/{eventType}")
    @Operation(
        summary = "Get events by type",
        description = "Retrieves all events of a specific type",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun getEventsByType(
        @Parameter(description = "Event type", example = "OrderCreatedEvent")
        @PathVariable eventType: String,
    ): ResponseEntity<List<InboxEvent>> =
        ResponseEntity.ok(inboxService.getEventsByType(eventType))
}
