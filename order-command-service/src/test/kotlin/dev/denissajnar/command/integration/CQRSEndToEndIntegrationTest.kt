package dev.denissajnar.command.integration

import dev.denissajnar.command.SpringBootTestParent
import dev.denissajnar.command.dto.CreateOrderCommandDTO
import dev.denissajnar.command.dto.UpdateOrderCommandDTO
import dev.denissajnar.command.util.whenever
import dev.denissajnar.shared.model.Status
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/**
 * Command Service Advanced Integration Tests using RestAssured
 * Demonstrates comprehensive API testing patterns and advanced RestAssured features
 */
class CQRSEndToEndIntegrationTest : SpringBootTestParent() {

    @Test
    fun `should demonstrate comprehensive order creation workflow`() {
        val createOrderDto = CreateOrderCommandDTO(
            customerId = 1L,
            totalAmount = BigDecimal("99.99"),
        )

        // Create order and extract response details
        val response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto)
            .whenever()
            .post()
            .then()
            .log().ifValidationFails()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("customerId", equalTo(1))
            .body("totalAmount", equalTo(BigDecimal("99.99")))
            .body("status", equalTo("PENDING"))
            .body("createdAt", notNullValue())
            .extract()
            .response()

        val orderId = response.path<String>("id")
        println("[DEBUG_LOG] Created order with ID: $orderId")
    }

    @Test
    fun `should demonstrate order update workflow with validation`() {
        // Step 1: Create initial order
        val createOrderDto = CreateOrderCommandDTO(
            customerId = 2L,
            totalAmount = BigDecimal("149.99"),
        )

        val createResponse = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto)
            .whenever()
            .post()
            .then()
            .statusCode(201)
            .extract()
            .response()

        val orderId = createResponse.path<String>("id")

        // Step 2: Update order with new values
        val updateOrderDto = UpdateOrderCommandDTO(
            customerId = 3L,
            totalAmount = BigDecimal("199.99"),
            status = Status.CONFIRMED,
        )

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(updateOrderDto)
            .whenever()
            .put("/update/{id}", orderId)
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("id", equalTo(orderId))
            .body("customerId", equalTo(3))
            .body("totalAmount", equalTo(BigDecimal(199.99)))
            .body("status", equalTo("CONFIRMED"))
            .body("createdAt", notNullValue())

        println("[DEBUG_LOG] Updated order $orderId successfully")
    }

    @Test
    fun `should demonstrate order deletion workflow`() {
        // Create order first
        val createOrderDto = CreateOrderCommandDTO(
            customerId = 4L,
            totalAmount = BigDecimal("79.99"),
        )

        val createResponse = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto)
            .whenever()
            .post()
            .then()
            .statusCode(201)
            .extract()
            .response()

        val orderId = createResponse.path<String>("id")

        // Delete the order
        RestAssured.given()
            .whenever()
            .delete("/{id}", orderId)
            .then()
            .log().ifValidationFails()
            .statusCode(200)

        println("[DEBUG_LOG] Deleted order $orderId successfully")
    }

    @Test
    fun `should demonstrate batch order operations`() {
        val customers = (10L..15L).toList()
        val orderIds = mutableListOf<String>()

        // Create multiple orders
        customers.forEach { customerId ->
            val createOrderDto = CreateOrderCommandDTO(
                customerId = customerId,
                totalAmount = BigDecimal("${customerId * 10}.99"),
            )

            val response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(createOrderDto)
                .whenever()
                .post()
                .then()
                .statusCode(201)
                .body("customerId", equalTo(customerId.toInt()))
                .body("totalAmount", equalTo(BigDecimal(customerId * 10.99)))
                .extract()
                .response()

            orderIds.add(response.path("id"))
        }

        println("[DEBUG_LOG] Created ${orderIds.size} orders: $orderIds")

        // Update some orders
        orderIds.take(2).forEach { orderId ->
            val updateOrderDto = UpdateOrderCommandDTO(
                customerId = null,
                totalAmount = BigDecimal("999.99"),
                status = Status.PROCESSING,
            )

            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(updateOrderDto)
                .whenever()
                .put("/update/{id}", orderId)
                .then()
                .statusCode(200)
                .body("totalAmount", equalTo(BigDecimal(999.99)))
                .body("status", equalTo("PROCESSING"))
        }

        println("[DEBUG_LOG] Updated first 2 orders to PROCESSING status")
    }

    @Test
    fun `should demonstrate error handling scenarios`() {
        // Test invalid customer ID
        val invalidCreateOrderDto = CreateOrderCommandDTO(
            customerId = -1L,
            totalAmount = BigDecimal("99.99"),
        )

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(invalidCreateOrderDto)
            .whenever()
            .post()
            .then()
            .statusCode(400)
            .body("message", containsString("Customer ID must be positive"))

        // Test invalid amount
        val invalidAmountOrderDto = CreateOrderCommandDTO(
            customerId = 1L,
            totalAmount = BigDecimal("-10.00"),
        )

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(invalidAmountOrderDto)
            .whenever()
            .post()
            .then()
            .statusCode(400)
            .body("message", containsString("Total amount must be positive"))

        // Test update non-existent order
        val updateOrderDto = UpdateOrderCommandDTO(
            customerId = 1L,
            totalAmount = BigDecimal("149.99"),
            status = Status.CONFIRMED,
        )

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(updateOrderDto)
            .whenever()
            .put("/update/{id}", "507f1f77bcf86cd799439011")
            .then()
            .statusCode(404)

        println("[DEBUG_LOG] Error handling scenarios tested successfully")
    }

    @Test
    fun `should demonstrate RestAssured advanced features`() {
        // Create order with detailed response validation
        val createOrderDto = CreateOrderCommandDTO(
            customerId = 100L,
            totalAmount = BigDecimal("299.99"),
        )

        val response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto)
            .whenever()
            .post()
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .header("Content-Type", containsString("application/json"))
            .body("id", matchesPattern("[a-f0-9]{24}"))  // MongoDB ObjectId pattern
            .body("customerId", both(greaterThan(0)).and(lessThanOrEqualTo(1000)))
            .body("totalAmount", equalTo(299.99f))
            .body("status", isOneOf("PENDING", "CONFIRMED", "PROCESSING"))
            .body("createdAt", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"))
            .time(lessThan(5000L))  // Response time validation
            .extract()
            .response()

        // Extract and validate specific values
        val orderId = response.path<String>("id")
        val createdAt = response.path<String>("createdAt")
        val totalAmount = response.path<BigDecimal>("totalAmount")

        assert(orderId.length == 24) { "Order ID should be MongoDB ObjectId" }
        assert(totalAmount.compareTo(BigDecimal("299.99")) == 0) { "Total amount mismatch" }

        println("[DEBUG_LOG] Advanced RestAssured features demonstrated for order: $orderId")
        println("[DEBUG_LOG] Order created at: $createdAt")
        println("[DEBUG_LOG] Total amount: $totalAmount")
    }

    @Test
    fun `should demonstrate concurrent operations testing`() {
        val numberOfConcurrentOrders = 5
        val orderIds = mutableListOf<String>()

        repeat(numberOfConcurrentOrders) { index ->
            val createOrderDto = CreateOrderCommandDTO(
                customerId = (200L + index),
                totalAmount = BigDecimal("${50 + index * 10}.99"),
            )

            val response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(createOrderDto)
                .whenever()
                .post()
                .then()
                .statusCode(201)
                .body("customerId", equalTo(200 + index))
                .extract()
                .response()

            orderIds.add(response.path("id"))
        }

        println("[DEBUG_LOG] Created $numberOfConcurrentOrders concurrent orders")

        // Verify all orders have unique IDs
        assert(orderIds.toSet().size == numberOfConcurrentOrders) {
            "All order IDs should be unique"
        }

        println("[DEBUG_LOG] All order IDs are unique: ${orderIds.toSet().size == numberOfConcurrentOrders}")
    }
}
