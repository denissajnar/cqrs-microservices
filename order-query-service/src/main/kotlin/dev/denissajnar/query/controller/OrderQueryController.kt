package dev.denissajnar.query.controller

import dev.denissajnar.query.dto.OrderDTO
import dev.denissajnar.query.service.OrderQueryService
import dev.denissajnar.shared.model.Status
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST controller for order query operations
 * Handles read operations in the CQRS architecture
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order Queries", description = "Operations for querying orders")
class OrderQueryController(
    private val orderQueryService: OrderQueryService
) {

    /**
     * Retrieves an order by its ID
     * @param orderId the order identifier
     * @return the order or 404 if not found
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieves order details from the query side database")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Order found"),
            ApiResponse(responseCode = "404", description = "Order not found"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    fun getOrder(
        @Parameter(description = "Order ID", example = "60ded37c-69c2-4464-8933-10f8c453f846")
        @PathVariable orderId: UUID
    ): ResponseEntity<OrderDTO> =
        orderQueryService.getOrderById(orderId)
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
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    fun getOrdersByCustomer(
        @Parameter(description = "Customer ID", example = "1")
        @RequestParam customerId: Long
    ): ResponseEntity<List<OrderDTO>> =
        ResponseEntity.ok(orderQueryService.getOrdersByCustomer(customerId))

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
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    fun getOrdersByStatus(
        @Parameter(description = "Order status", example = "PENDING")
        @RequestParam status: Status
    ): ResponseEntity<List<OrderDTO>> =
        ResponseEntity.ok(orderQueryService.getOrdersByStatus(status))
}