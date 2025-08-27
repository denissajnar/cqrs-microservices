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
import org.springframework.web.bind.annotation.*

/**
 * REST controller for order query operations
 * Handles read operations in the CQRS architecture
 */
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
     * Retrieves orders by customer ID
     * @param customerId the customer identifier
     * @return list of orders for the customer
     */
    @GetMapping
    @Operation(summary = "Get orders by customer", description = "Retrieves all orders for a specific customer")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            ApiResponse(responseCode = "400", description = "Invalid customer ID"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun getOrdersByCustomer(
        @Parameter(description = "Customer ID", example = "1")
        @RequestParam customerId: Long,
    ): ResponseEntity<List<OrderQueryDTO>> {
        require(customerId >= 0) { "Customer ID must be positive" }
        return ResponseEntity.ok(orderQueryService.getOrdersByCustomer(customerId))
    }

    /**
     * Retrieves orders by status
     * @param status the order status
     * @return list of orders with the given status
     */
    @GetMapping("/by-status")
    @Operation(summary = "Get orders by status", description = "Retrieves all orders with a specific status")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            ApiResponse(responseCode = "400", description = "Invalid status"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun getOrdersByStatus(
        @Parameter(description = "OrderQuery status", example = "PENDING")
        @RequestParam status: Status,
    ): ResponseEntity<List<OrderQueryDTO>> =
        ResponseEntity.ok(orderQueryService.getOrdersByStatus(status))
}
