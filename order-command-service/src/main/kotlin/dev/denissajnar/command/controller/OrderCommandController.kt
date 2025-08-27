package dev.denissajnar.command.controller

import dev.denissajnar.command.dto.CreateOrderCommandDTO
import dev.denissajnar.command.dto.OrderResponseDTO
import dev.denissajnar.command.dto.UpdateOrderCommandDTO
import dev.denissajnar.command.service.OrderCommandService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for order command operations
 * Handles write operations in the CQRS architecture
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order Commands", description = "Operations for creating and managing orders")
class OrderCommandController(
    private val orderCommandService: OrderCommandService,
) {

    /**
     * Creates a new order
     * @param dto the order creation request
     * @return the created order response
     */
    @PostMapping
    @Operation(
        summary = "Create new order",
        description = "Creates a new order and publishes an event for CQRS synchronization",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Order created successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request data"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun createOrder(
        @Valid @RequestBody dto: CreateOrderCommandDTO,
    ): ResponseEntity<OrderResponseDTO> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(orderCommandService.createOrder(dto))

    /**
     * Updates an existing order
     * @param id the order identifier
     * @param dto the order update request
     * @return the updated order response
     */
    @PutMapping("/update/{id}")
    @Operation(
        summary = "Update order",
        description = "Updates an existing order and publishes an event for CQRS synchronization",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Order updated successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request data"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun updateOrder(
        @PathVariable id: String, @Valid @RequestBody dto: UpdateOrderCommandDTO,
    ): ResponseEntity<OrderResponseDTO> =
        ResponseEntity.status(HttpStatus.OK)
            .body(orderCommandService.updateOrder(id, dto))

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete order",
        description = "Deletes an existing order",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Order deleted successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request data"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ],
    )
    fun deleteOrder(@PathVariable id: String): ResponseEntity<Unit> {
        orderCommandService.deleteOrder(id)

        return ResponseEntity.status(HttpStatus.OK).build()
    }

}
