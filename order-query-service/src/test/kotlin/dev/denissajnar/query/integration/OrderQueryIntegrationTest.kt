package dev.denissajnar.query.integration

import dev.denissajnar.query.entity.Order
import dev.denissajnar.query.repository.OrderRepository
import dev.denissajnar.shared.model.Status
import io.restassured.RestAssured
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * Integration tests for Order Query Service
 * Uses TestContainers for PostgreSQL
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderQueryIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var orderRepository: OrderRepository

    companion object {
        @Container
        val postgreSQLContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("orders_query")
            .withUsername("user")
            .withPassword("password")

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgreSQLContainer.jdbcUrl }
            registry.add("spring.datasource.username") { postgreSQLContainer.username }
            registry.add("spring.datasource.password") { postgreSQLContainer.password }
        }
    }

    @BeforeEach
    fun setup() {
        RestAssured.port = port
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()

        // Clean database before each test
        orderRepository.deleteAll()
    }

    @Test
    fun `should get order by ID successfully`() {
        // Given - create test data
        val testOrderId = UUID.randomUUID()
        val testOrder = Order(
            id = testOrderId,
            customerId = 1L,
            totalAmount = BigDecimal("99.99"),
            status = Status.PENDING,
            createdAt = Instant.now()
        )
        val savedOrder = orderRepository.save(testOrder)

        // When & Then
        RestAssured.given()
            .`when`()
            .get("/api/v1/orders/${savedOrder.id}")
            .then()
            .statusCode(200)
            .body("id", equalTo(savedOrder.id.toString()))
            .body("customerId", equalTo(1))
            .body("totalAmount", equalTo(99.99f))
            .body("status", equalTo(Status.PENDING.name))
    }

    @Test
    fun `should return 404 for non-existent order`() {
        RestAssured.given()
            .`when`()
            .get("/api/v1/orders/non-existent-id")
            .then()
            .statusCode(404)
    }

    @Test
    fun `should get orders by customer ID successfully`() {
        // Given - create test data for different customers
        val customer1Order1 = Order(
            id = UUID.randomUUID(),
            customerId = 1L,
            totalAmount = BigDecimal("50.00"),
            status = Status.PENDING,
            createdAt = Instant.now().minusMillis(72000000)
        )

        val customer1Order2 = Order(
            id = UUID.randomUUID(),
            customerId = 1L,
            totalAmount = BigDecimal("75.00"),
            status = Status.COMPLETED,
            createdAt = Instant.now().minusMillis(36000000)
        )

        val customer2Order = Order(
            id = UUID.randomUUID(),
            customerId = 2L,
            totalAmount = BigDecimal("100.00"),
            status = Status.PENDING,
            createdAt = Instant.now()
        )

        orderRepository.saveAll(listOf(customer1Order1, customer1Order2, customer2Order))

        // When & Then - get orders for customer 1
        RestAssured.given()
            .queryParam("customerId", 1)
            .`when`()
            .get("/api/v1/orders")
            .then()
            .statusCode(200)
            .body("size()", equalTo(2))
            .body("[0].customerId", equalTo(1))
            .body("[1].customerId", equalTo(1))
    }

    @Test
    fun `should return empty list for customer with no orders`() {
        RestAssured.given()
            .queryParam("customerId", 999)
            .`when`()
            .get("/api/v1/orders")
            .then()
            .statusCode(200)
            .body("size()", equalTo(0))
    }

    @Test
    fun `should get orders by status successfully`() {
        // Given - create test data with different statuses
        val pendingOrder1 = Order(
            id = UUID.randomUUID(),
            customerId = 1L,
            totalAmount = BigDecimal("50.00"),
            status = Status.PENDING,
            createdAt = Instant.now()
        )

        val pendingOrder2 = Order(
            id = UUID.randomUUID(),
            customerId = 2L,
            totalAmount = BigDecimal("75.00"),
            status = Status.PENDING,
            createdAt = Instant.now()
        )

        val completedOrder = Order(
            id = UUID.randomUUID(),
            customerId = 1L,
            totalAmount = BigDecimal("100.00"),
            status = Status.COMPLETED,
            createdAt = Instant.now()
        )

        orderRepository.saveAll(listOf(pendingOrder1, pendingOrder2, completedOrder))

        // When & Then - get orders with PENDING status
        RestAssured.given()
            .queryParam("status", Status.PENDING)
            .`when`()
            .get("/api/v1/orders/by-status")
            .then()
            .statusCode(200)
            .body("size()", equalTo(2))
            .body("[0].status", equalTo(Status.PENDING.name))
            .body("[1].status", equalTo(Status.PENDING.name))
    }
}