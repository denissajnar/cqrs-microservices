package dev.denissajnar.query.unit

import dev.denissajnar.query.entity.Order
import dev.denissajnar.query.repository.OrderRepository
import dev.denissajnar.query.service.OrderQueryService
import dev.denissajnar.shared.model.Status
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * Unit tests for OrderQueryService using MockK
 * Tests read operations with mocked repository
 */
class OrderQueryServiceTest {

    private val orderRepository = mockk<OrderRepository>()
    private val orderQueryService = OrderQueryService(orderRepository)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Test
    fun `should return order when found by ID`() {
        // Given
        val orderId = UUID.randomUUID()
        val testOrder = Order(
            id = orderId,
            customerId = 1L,
            totalAmount = BigDecimal("99.99"),
            status = Status.PENDING,
            createdAt = Instant.now()
        )

        every { orderRepository.findById(orderId) } returns Optional.of(testOrder)

        // When
        val result = orderQueryService.getOrderById(orderId)

        // Then
        assertNotNull(result)
        assertEquals(orderId, result!!.id)
        assertEquals(1L, result.customerId)
        assertEquals(BigDecimal("99.99"), result.totalAmount)
        assertEquals(Status.PENDING, result.status)
        assertNotNull(result.createdAt)

        verify { orderRepository.findById(orderId) }
    }

    @Test
    fun `should return null when order not found by ID`() {
        // Given
        val orderId: UUID = UUID.randomUUID()

        every { orderRepository.findByIdOrNull(orderId) } returns null

        // When
        val result = orderQueryService.getOrderById(orderId)

        // Then
        assertNull(result)

        verify { orderRepository.findByIdOrNull(orderId) }
    }

    @Test
    fun `should return orders for customer when found`() {
        // Given
        val customerId = 1L
        val order1 = Order(
            id = UUID.randomUUID(),
            customerId = customerId,
            totalAmount = BigDecimal("50.00"),
            status = Status.PENDING,
            createdAt = Instant.now().minusMillis(7200000)
        )

        val order2 = Order(
            id = UUID.randomUUID(),
            customerId = customerId,
            totalAmount = BigDecimal("75.00"),
            status = Status.COMPLETED,
            createdAt = Instant.now().minusMillis(3600000)
        )

        val orders = listOf(order2, order1) // Ordered by created date desc

        every { orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId) } returns orders

        // When
        val result = orderQueryService.getOrdersByCustomer(customerId)

        // Then
        assertEquals(2, result.size)
        assertEquals(order2.id, result[0].id)
        assertEquals(order1.id, result[1].id)
        assertEquals(customerId, result[0].customerId)
        assertEquals(customerId, result[1].customerId)

        verify { orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId) }
    }

    @Test
    fun `should return empty list when no orders found for customer`() {
        // Given
        val customerId = 999L

        every { orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId) } returns emptyList()

        // When
        val result = orderQueryService.getOrdersByCustomer(customerId)

        // Then
        assertTrue(result.isEmpty())

        verify { orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId) }
    }

    @Test
    fun `should return orders by status when found`() {
        // Given
        val status = Status.PENDING
        val order1 = Order(
            id = UUID.randomUUID(),
            customerId = 1L,
            totalAmount = BigDecimal("50.00"),
            status = status,
            createdAt = Instant.now()
        )

        val order2 = Order(
            id = UUID.randomUUID(),
            customerId = 2L,
            totalAmount = BigDecimal("75.00"),
            status = status,
            createdAt = Instant.now()
        )

        val orders = listOf(order1, order2)

        every { orderRepository.findByStatus(status) } returns orders

        // When
        val result = orderQueryService.getOrdersByStatus(status)

        // Then
        assertEquals(2, result.size)
        assertEquals(order1.id, result[0].id)
        assertEquals(order2.id, result[1].id)
        assertEquals(status, result[0].status)
        assertEquals(status, result[1].status)

        verify { orderRepository.findByStatus(status) }
    }

    @Test
    fun `should return empty list when no orders found by status`() {
        // Given
        val status = Status.COMPLETED

        every { orderRepository.findByStatus(status) } returns emptyList()

        // When
        val result = orderQueryService.getOrdersByStatus(status)

        // Then
        assertTrue(result.isEmpty())

        verify { orderRepository.findByStatus(status) }
    }

    @Test
    fun `should handle repository exception when finding by ID`() {
        // Given
        val orderId = UUID.randomUUID()

        every { orderRepository.findById(orderId) } throws RuntimeException("Database error")

        // When & Then
        assertThrows(RuntimeException::class.java) {
            orderQueryService.getOrderById(orderId)
        }

        verify { orderRepository.findById(orderId) }
    }

    @Test
    fun `should handle repository exception when finding by customer`() {
        // Given
        val customerId = 1L

        every { orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId) } throws RuntimeException("Database error")

        // When & Then
        assertThrows(RuntimeException::class.java) {
            orderQueryService.getOrdersByCustomer(customerId)
        }

        verify { orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId) }
    }

    @Test
    fun `should handle repository exception when finding by status`() {
        // Given
        val status = Status.PENDING

        every { orderRepository.findByStatus(status) } throws RuntimeException("Database error")

        // When & Then
        assertThrows(RuntimeException::class.java) {
            orderQueryService.getOrdersByStatus(status)
        }

        verify { orderRepository.findByStatus(status) }
    }
}