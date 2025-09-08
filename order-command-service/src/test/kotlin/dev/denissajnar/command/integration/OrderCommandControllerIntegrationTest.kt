package dev.denissajnar.command.integration

import dev.denissajnar.command.SpringBootTestParent
import dev.denissajnar.command.dto.request.CreateOrderCommandRequest
import dev.denissajnar.command.dto.request.UpdateOrderCommandRequest
import dev.denissajnar.command.util.whenever
import dev.denissajnar.shared.model.Status
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/**
 * Integration tests for Order Command Service REST API using RestAssured
 * Tests all CRUD operations with both happy path and error scenarios
 */
class OrderCommandControllerIntegrationTest : SpringBootTestParent() {

    @Test
    fun `should create order successfully with valid data`() {
        val createOrderDto = CreateOrderCommandRequest(
            customerId = 1L,
            totalAmount = BigDecimal("99.99"),
        )

        RestAssured.given()
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
    fun `should return 400 when creating order with invalid customer id`() {
        val createOrderDto = CreateOrderCommandRequest(
            customerId = -1L,
            totalAmount = BigDecimal("99.99"),
        )

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto)
            .whenever()
            .post()
            .then()
            .log().ifValidationFails()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("details[0]", containsString("Customer ID must be positive"))
    }

    @Test
    fun `should return 400 when creating order with invalid total amount`() {
        val createOrderDto = CreateOrderCommandRequest(
            customerId = 1L,
            totalAmount = BigDecimal("-10.00"),
        )

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto)
            .whenever()
            .post()
            .then()
            .log().ifValidationFails()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("details[0]", containsString("Total amount must be positive"))
    }

    @Test
    fun `should update order successfully with valid data`() {
        val createOrderDto = CreateOrderCommandRequest(
            customerId = 1L,
            totalAmount = BigDecimal("99.99"),
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

        val updateOrderDto = UpdateOrderCommandRequest(
            customerId = 2L,
            totalAmount = BigDecimal("149.99"),
            status = Status.CONFIRMED,
        )

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(updateOrderDto)
            .whenever()
            .put("/{id}", orderId)
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("id", not(equalTo(orderId)))
            .body("customerId", equalTo(2))
            .body("totalAmount", equalTo(149.99f))
            .body("status", equalTo("CONFIRMED"))
            .body("createdAt", notNullValue())

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
    fun `should return 400 when updating order with invalid customer id`() {
        val createOrderDto = CreateOrderCommandRequest(
            customerId = 1L,
            totalAmount = BigDecimal("99.99"),
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

        val updateOrderDto = UpdateOrderCommandRequest(
            customerId = -1L,
            totalAmount = BigDecimal("149.99"),
            status = Status.CONFIRMED,
        )

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(updateOrderDto)
            .whenever()
            .put("/{id}", orderId)
            .then()
            .log().ifValidationFails()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("details[0]", containsString("Customer ID must be positive"))
    }

    @Test
    fun `should return 404 when updating non-existent order`() {
        val updateOrderDto = UpdateOrderCommandRequest(
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
            .log().ifValidationFails()
            .statusCode(404)
    }

    @Test
    fun `should delete order successfully`() {
        val createOrderDto = CreateOrderCommandRequest(
            customerId = 1L,
            totalAmount = BigDecimal("99.99"),
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
    fun `should return 404 when deleting non-existent order`() {
        RestAssured.given()
            .whenever()
            .delete("/{id}", "507f1f77bcf86cd799439011")
            .then()
            .log().ifValidationFails()
            .statusCode(404)
    }

    @Test
    fun `should handle concurrent order creation`() {
        val createOrderDto1 = CreateOrderCommandRequest(
            customerId = 1L,
            totalAmount = BigDecimal("99.99"),
        )

        val createOrderDto2 = CreateOrderCommandRequest(
            customerId = 2L,
            totalAmount = BigDecimal("149.99"),
        )

        val response1 = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto1)
            .whenever()
            .post()
            .then()
            .statusCode(201)
            .extract()
            .response()

        val response2 = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto2)
            .whenever()
            .post()
            .then()
            .statusCode(201)
            .extract()
            .response()

        val orderId1 = response1.path<String>("id")
        val orderId2 = response2.path<String>("id")

        assert(orderId1 != orderId2)

        val outboxEvents = outboxEventRepository.findAll()
        assert(outboxEvents.isNotEmpty()) { "Outbox should contain events after concurrent order creation" }

        val orderEvents = outboxEvents.filter { it.eventType == "OrderEvent" }
        assert(orderEvents.size >= 2) { "Outbox should contain at least 2 OrderEvent entries for concurrent creation" }

        orderEvents.forEach { event ->
            assert(event.eventId.isNotEmpty()) { "Event should have a valid eventId" }
            assert(event.eventPayload.isNotEmpty()) { "Event should have a valid payload" }
            assert(event.routingKey.isNotEmpty()) { "Event should have a valid routing key" }
            assert(event.exchange.isNotEmpty()) { "Event should have a valid exchange" }
        }
    }
}
