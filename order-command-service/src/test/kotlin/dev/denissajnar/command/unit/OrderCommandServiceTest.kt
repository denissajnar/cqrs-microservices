package dev.denissajnar.command.unit

import dev.denissajnar.command.domain.OrderCommand
import dev.denissajnar.command.dto.CreateOrderDTO
import dev.denissajnar.command.messaging.EventPublisher
import dev.denissajnar.command.repository.OrderCommandRepository
import dev.denissajnar.command.service.OrderCommandService
import dev.denissajnar.shared.events.OrderCreatedEvent
import dev.denissajnar.shared.model.Status
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.*

/**
 * Unit tests for OrderCommandService using MockK
 * Tests business logic with mocked dependencies
 */
class OrderCommandServiceTest {

    private val orderRepository = mockk<OrderCommandRepository>()
    private val eventPublisher = mockk<EventPublisher>()
    private val orderCommandService = OrderCommandService(orderRepository, eventPublisher)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Test
    fun `should create order successfully and publish event`() {
        // Given
        val createOrderDTO = CreateOrderDTO(
            customerId = 1L,
            totalAmount = BigDecimal("99.99")
        )

        val savedOrder = OrderCommand(
            id = UUID.randomUUID(),
            customerId = 1L,
            totalAmount = BigDecimal("99.99"),
            status = Status.PENDING
        )

        every { orderRepository.save(any<OrderCommand>()) } returns savedOrder
        every { eventPublisher.publish(any<OrderCreatedEvent>()) } just runs

        // When
        val result = orderCommandService.createOrder(createOrderDTO)

        // Then
        assertEquals(savedOrder.id, result.id)
        assertEquals(1L, result.customerId)
        assertEquals(BigDecimal("99.99"), result.totalAmount)
        assertEquals(Status.PENDING, result.status)
        assertNotNull(result.createdAt)

        // Verify repository save was called
        verify {
            orderRepository.save(match { order ->
                order.customerId == 1L &&
                        order.totalAmount == BigDecimal("99.99") &&
                        order.status == Status.PENDING
            })
        }

        // Verify event was published
        verify {
            eventPublisher.publish(match { event ->
                event is OrderCreatedEvent &&
                        event.orderId == savedOrder.id &&
                        event.customerId == 1L &&
                        event.totalAmount == BigDecimal("99.99") &&
                        event.status == Status.PENDING
            })
        }
    }

    @Test
    fun `should throw exception for negative customer ID`() {
        // Given
        val invalidDTO = CreateOrderDTO(
            customerId = -1L,
            totalAmount = BigDecimal("99.99")
        )

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            orderCommandService.createOrder(invalidDTO)
        }

        assertEquals("Customer ID must be positive", exception.message)

        // Verify no repository or event publisher calls
        verify(exactly = 0) { orderRepository.save(any()) }
        verify(exactly = 0) { eventPublisher.publish(any()) }
    }

    @Test
    fun `should throw exception for zero customer ID`() {
        // Given
        val invalidDTO = CreateOrderDTO(
            customerId = 0L,
            totalAmount = BigDecimal("99.99")
        )

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            orderCommandService.createOrder(invalidDTO)
        }

        assertEquals("Customer ID must be positive", exception.message)

        // Verify no repository or event publisher calls
        verify(exactly = 0) { orderRepository.save(any()) }
        verify(exactly = 0) { eventPublisher.publish(any()) }
    }

    @Test
    fun `should throw exception for zero total amount`() {
        // Given
        val invalidDTO = CreateOrderDTO(
            customerId = 1L,
            totalAmount = BigDecimal.ZERO
        )

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            orderCommandService.createOrder(invalidDTO)
        }

        assertEquals("Total amount must be positive", exception.message)

        // Verify no repository or event publisher calls
        verify(exactly = 0) { orderRepository.save(any()) }
        verify(exactly = 0) { eventPublisher.publish(any()) }
    }

    @Test
    fun `should throw exception for negative total amount`() {
        // Given
        val invalidDTO = CreateOrderDTO(
            customerId = 1L,
            totalAmount = BigDecimal("-10.00")
        )

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            orderCommandService.createOrder(invalidDTO)
        }

        assertEquals("Total amount must be positive", exception.message)

        // Verify no repository or event publisher calls
        verify(exactly = 0) { orderRepository.save(any()) }
        verify(exactly = 0) { eventPublisher.publish(any()) }
    }

    @Test
    fun `should throw exception for total amount with more than 2 decimal places`() {
        // Given
        val invalidDTO = CreateOrderDTO(
            customerId = 1L,
            totalAmount = BigDecimal("99.999")
        )

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            orderCommandService.createOrder(invalidDTO)
        }

        assertEquals("Total amount cannot have more than 2 decimal places", exception.message)

        // Verify no repository or event publisher calls
        verify(exactly = 0) { orderRepository.save(any()) }
        verify(exactly = 0) { eventPublisher.publish(any()) }
    }

    @Test
    fun `should handle repository exception gracefully`() {
        // Given
        val createOrderDTO = CreateOrderDTO(
            customerId = 1L,
            totalAmount = BigDecimal("99.99")
        )

        every { orderRepository.save(any<OrderCommand>()) } throws RuntimeException("Database error")

        // When & Then
        assertThrows<RuntimeException> {
            orderCommandService.createOrder(createOrderDTO)
        }

        // Verify event publisher was not called due to exception
        verify(exactly = 0) { eventPublisher.publish(any()) }
    }
}