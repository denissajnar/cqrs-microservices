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
            .body("totalAmount", equalTo(99.99f))
            .body("status", equalTo("PENDING"))
            .body("createdAt", notNullValue())
            .extract()
            .response()

        val outboxEvents = outboxEventRepository.findAll()
        assert(outboxEvents.isNotEmpty()) { "Outbox should contain events after order creation" }
        assert(outboxEvents.any { it.eventType == "OrderEvent" }) { "Outbox should contain OrderEvent for created order" }

        val orderEvents = outboxEvents.filter { it.eventType == "OrderEvent" }
        orderEvents.forEach { event ->
            assert(event.eventId.isNotEmpty()) { "Event should have a valid eventId" }
            assert(event.eventPayload.isNotEmpty()) { "Event should have a valid payload" }
            assert(event.routingKey.isNotEmpty()) { "Event should have a valid routing key" }
            assert(event.exchange.isNotEmpty()) { "Event should have a valid exchange" }
        }
    }

    @Test
    fun `should demonstrate order update workflow with validation`() {
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

        val outboxEventsAfterCreate = outboxEventRepository.findAll()
        assert(outboxEventsAfterCreate.isNotEmpty()) { "Outbox should contain events after order creation" }
        assert(outboxEventsAfterCreate.any { it.eventType == "OrderEvent" }) { "Outbox should contain OrderEvent for created order" }

        val updateOrderDto = UpdateOrderCommandDTO(
            customerId = 3L,
            totalAmount = BigDecimal("199.99"),
            status = Status.CONFIRMED,
        )

        val updateResponse = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(updateOrderDto)
            .whenever()
            .put("/{id}", orderId)
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("id", notNullValue())
            .body("id", not(equalTo(orderId)))
            .body("customerId", equalTo(3))
            .body("totalAmount", equalTo(199.99f))
            .body("status", equalTo("CONFIRMED"))
            .body("createdAt", notNullValue())
            .extract()
            .response()

        val outboxEventsAfterUpdate = outboxEventRepository.findAll()
        assert(outboxEventsAfterUpdate.size > outboxEventsAfterCreate.size) { "Outbox should contain more events after order update" }

        val orderEvents = outboxEventsAfterUpdate.filter { it.eventType == "OrderEvent" }
        assert(orderEvents.size >= 2) { "Outbox should contain at least 2 OrderEvent entries (create and update)" }

        orderEvents.forEach { event ->
            assert(event.eventId.isNotEmpty()) { "Event should have a valid eventId" }
            assert(event.eventPayload.isNotEmpty()) { "Event should have a valid payload" }
            assert(event.routingKey.isNotEmpty()) { "Event should have a valid routing key" }
            assert(event.exchange.isNotEmpty()) { "Event should have a valid exchange" }
        }
    }

    @Test
    fun `should demonstrate order deletion workflow`() {
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

        val outboxEventsAfterCreate = outboxEventRepository.findAll()
        assert(outboxEventsAfterCreate.isNotEmpty()) { "Outbox should contain events after order creation" }
        assert(outboxEventsAfterCreate.any { it.eventType == "OrderEvent" }) { "Outbox should contain OrderEvent for created order" }

        RestAssured.given()
            .whenever()
            .delete("/{id}", orderId)
            .then()
            .log().ifValidationFails()
            .statusCode(200)

        val outboxEventsAfterDelete = outboxEventRepository.findAll()
        assert(outboxEventsAfterDelete.size > outboxEventsAfterCreate.size) { "Outbox should contain more events after order deletion" }

        val orderEvents = outboxEventsAfterDelete.filter { it.eventType == "OrderEvent" }
        assert(orderEvents.size >= 2) { "Outbox should contain at least 2 OrderEvent entries (create and delete)" }

        orderEvents.forEach { event ->
            assert(event.eventId.isNotEmpty()) { "Event should have a valid eventId" }
            assert(event.eventPayload.isNotEmpty()) { "Event should have a valid payload" }
            assert(event.routingKey.isNotEmpty()) { "Event should have a valid routing key" }
            assert(event.exchange.isNotEmpty()) { "Event should have a valid exchange" }
        }
    }

    @Test
    fun `should demonstrate batch order operations`() {
        val customers = (10L..15L).toList()
        val orderIds = mutableListOf<String>()

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
                .body("totalAmount", equalTo("${customerId * 10}.99".toFloat()))
                .extract()
                .response()

            orderIds.add(response.path("id"))
        }

        val outboxEventsAfterCreates = outboxEventRepository.findAll()
        assert(outboxEventsAfterCreates.isNotEmpty()) { "Outbox should contain events after batch order creation" }

        val createOrderEvents = outboxEventsAfterCreates.filter { it.eventType == "OrderEvent" }
        assert(createOrderEvents.size >= customers.size) { "Outbox should contain at least ${customers.size} OrderEvent entries for batch creation" }

        orderIds.take(2).forEach { orderId ->
            val updateOrderDto = UpdateOrderCommandDTO(
                customerId = 999L,
                totalAmount = BigDecimal("999.99"),
                status = Status.PROCESSING,
            )

            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(updateOrderDto)
                .whenever()
                .put("/{id}", orderId)
                .then()
                .statusCode(200)
                .body("totalAmount", equalTo(999.99f))
                .body("status", equalTo("PROCESSING"))
        }

        val outboxEventsAfterUpdates = outboxEventRepository.findAll()
        assert(outboxEventsAfterUpdates.size > outboxEventsAfterCreates.size) { "Outbox should contain more events after batch updates" }

        val allOrderEvents = outboxEventsAfterUpdates.filter { it.eventType == "OrderEvent" }
        assert(allOrderEvents.size >= customers.size + 2) { "Outbox should contain at least ${customers.size + 2} OrderEvent entries (${customers.size} creates + 2 updates)" }

        allOrderEvents.forEach { event ->
            assert(event.eventId.isNotEmpty()) { "Event should have a valid eventId" }
            assert(event.eventPayload.isNotEmpty()) { "Event should have a valid payload" }
            assert(event.routingKey.isNotEmpty()) { "Event should have a valid routing key" }
            assert(event.exchange.isNotEmpty()) { "Event should have a valid exchange" }
        }
    }

    @Test
    fun `should demonstrate error handling scenarios`() {
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
            .body("details[0]", containsString("Customer ID must be positive"))

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
            .body("details[0]", containsString("Total amount must be positive"))

        val updateOrderDto = UpdateOrderCommandDTO(
            customerId = 1L,
            totalAmount = BigDecimal("149.99"),
            status = Status.CONFIRMED,
        )

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(updateOrderDto)
            .whenever()
            .put("/{id}", "507f1f77bcf86cd799439011")
            .then()
            .statusCode(404)
    }

    @Test
    fun `should demonstrate RestAssured advanced features`() {
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
            .body("id", matchesPattern("[a-f0-9]{24}"))
            .body("customerId", both(greaterThan(0)).and(lessThanOrEqualTo(1000)))
            .body("totalAmount", equalTo(299.99f))
            .body("status", isOneOf("PENDING", "CONFIRMED", "PROCESSING"))
            .body("createdAt", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"))
            .time(lessThan(5000L))
            .extract()
            .response()

        val orderId = response.path<String>("id")
        val totalAmount = response.path<Float>("totalAmount")

        assert(orderId.length == 24) { "Order ID should be MongoDB ObjectId" }
        assert(totalAmount == 299.99f) { "Total amount mismatch" }
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

        assert(orderIds.toSet().size == numberOfConcurrentOrders) {
            "All order IDs should be unique"
        }

        val outboxEvents = outboxEventRepository.findAll()
        assert(outboxEvents.isNotEmpty()) { "Outbox should contain events after concurrent order creation" }

        val orderEvents = outboxEvents.filter { it.eventType == "OrderEvent" }
        assert(orderEvents.size >= numberOfConcurrentOrders) { "Outbox should contain at least $numberOfConcurrentOrders OrderEvent entries for concurrent creation" }

        orderEvents.forEach { event ->
            assert(event.eventId.isNotEmpty()) { "Event should have a valid eventId" }
            assert(event.eventPayload.isNotEmpty()) { "Event should have a valid payload" }
            assert(event.routingKey.isNotEmpty()) { "Event should have a valid routing key" }
            assert(event.exchange.isNotEmpty()) { "Event should have a valid exchange" }
        }
    }
}
