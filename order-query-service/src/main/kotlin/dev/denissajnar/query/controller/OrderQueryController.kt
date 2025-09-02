package dev.denissajnar.query.controller

import dev.denissajnar.query.dto.OrderQueryDTO
import dev.denissajnar.query.service.OrderQueryService
import dev.denissajnar.shared.model.Status
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

/**
 * REST controller for order query operations
 * Handles read operations in the CQRS architecture
 */
@Validated
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "OrderQuery Queries", description = "Operations for querying orders")
class OrderQueryController(
    private val orderQueryService: OrderQueryService,
) {

    /**
     * Retrieves an order by its ID
     * @param id the order identifier
     * @return the order or 404 if not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieves order details from the query side database")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OrderQuery found"),
            ApiResponse(responseCode = "404", description = "OrderQuery not found"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun getOrder(
        @Parameter(description = "OrderQuery ID", example = "1")
        @PathVariable id: Long,
    ): ResponseEntity<OrderQueryDTO> =
        orderQueryService.findOrderById(id)
            ?.let { order -> ResponseEntity.ok(order) }
            ?: ResponseEntity.notFound().build()

    /**
     * Retrieves orders with optional filtering by customer ID and/or status
     * @param customerId optional customer identifier to filter by
     * @param status optional order status to filter by
     * @return list of orders matching the filter criteria
     */
    @GetMapping
    @Operation(
        summary = "Get orders with optional filtering",
        description = "Retrieves orders from the query side database with optional filtering by customer ID and/or status. If no filters are provided, returns all orders.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun getOrders(
        @Parameter(description = "Customer ID to filter by", example = "1")
        @RequestParam(required = false) customerId: Long?,
        @Parameter(description = "Order status to filter by", example = "PENDING")
        @RequestParam(required = false) status: Status?,
    ): ResponseEntity<List<OrderQueryDTO>> {
        return try {
            if (customerId != null && customerId < 0) {
                return ResponseEntity.badRequest().build()
            }

            val orders = when {
                customerId != null && status != null ->
                    orderQueryService.getOrdersByCustomerAndStatus(customerId, status)

                customerId != null ->
                    orderQueryService.getOrdersByCustomer(customerId)

                status != null ->
                    orderQueryService.getOrdersByStatus(status)

                else ->
                    orderQueryService.getAllOrders()
            }

            ResponseEntity.ok(orders)
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * Retrieves an order by its history ID
     * @param historyId the history identifier from command side
     * @return the order or 404 if not found
     */
    @GetMapping("/history/{historyId}")
    @Operation(
        summary = "Get order by history ID",
        description = "Retrieves order details using the history ID from the command side. The history ID is a UUID string that uniquely identifies an order across both command and query sides.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Order found and retrieved successfully"),
            ApiResponse(responseCode = "404", description = "Order not found with the given history ID"),
            ApiResponse(responseCode = "400", description = "Invalid or blank history ID provided"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun getOrderByHistoryId(
        @Parameter(
            description = "History ID from command side (UUID format)",
            example = "550e8400-e29b-41d4-a716-446655440000",
        )
        @PathVariable historyId: String,
    ): ResponseEntity<OrderQueryDTO> {
        return try {
            if (historyId.isBlank()) {
                return ResponseEntity.badRequest().build()
            }

            orderQueryService.findOrderByHistoryId(historyId)
                ?.let { order -> ResponseEntity.ok(order) }
                ?: ResponseEntity.notFound().build()
        } catch (_: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }
}
