package dev.denissajnar.command.controller

import dev.denissajnar.command.dto.CreateOrderDTO
import dev.denissajnar.command.dto.OrderResponseDTO
import dev.denissajnar.command.service.OrderCommandService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for order command operations
 * Handles write operations in the CQRS architecture
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order Commands", description = "Operations for creating and managing orders")
class OrderCommandController(
    private val orderCommandService: OrderCommandService
) {

    /**
     * Creates a new order
     * @param dto the order creation request
     * @return the created order response
     */
    @PostMapping
    @Operation(
        summary = "Create new order",
        description = "Creates a new order and publishes an event for CQRS synchronization"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Order created successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request data"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    fun createOrder(
        @Valid @RequestBody dto: CreateOrderDTO
    ): ResponseEntity<OrderResponseDTO> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(orderCommandService.createOrder(dto))
}