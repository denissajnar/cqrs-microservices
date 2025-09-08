package dev.denissajnar.command.integration

import dev.denissajnar.command.SpringBootTestParent
import dev.denissajnar.command.dto.request.CreateOrderCommandRequest
import dev.denissajnar.command.util.whenever
import dev.denissajnar.shared.events.EventType
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/**
 * Integration tests for Outbox Controller REST API using RestAssured
 * Tests all outbox monitoring operations with both happy path and error scenarios
 */
class OutboxControllerIntegrationTest : SpringBootTestParent() {

    @BeforeEach
    override fun setUp() {
        super.setUp()
        RestAssured.basePath = "/api/v1/outbox"
    }

    @Test
    fun `should get unprocessed events successfully`() {
        RestAssured.given()
            .whenever()
            .get("/unprocessed")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThanOrEqualTo(0))
    }

    @Test
    fun `should get outbox statistics successfully`() {
        RestAssured.given()
            .whenever()
            .get("/stats")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("totalEvents", greaterThanOrEqualTo(0))
            .body("processedEvents", greaterThanOrEqualTo(0))
            .body("unprocessedEvents", greaterThanOrEqualTo(0))
            .body("processingRate", notNullValue())
    }

    @Test
    fun `should get all outbox events successfully`() {
        RestAssured.given()
            .whenever()
            .get("")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThanOrEqualTo(0))
    }

    @Test
    fun `should create outbox events when creating orders`() {
        val initialStatsResponse = RestAssured.given()
            .whenever()
            .get("/stats")
            .then()
            .statusCode(200)
            .extract()
            .response()

        val initialTotalEvents = initialStatsResponse.path<Int>("totalEvents")

        val createOrderDto = CreateOrderCommandRequest(
            customerId = 1L,
            totalAmount = BigDecimal("99.99"),
        )

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto)
            .whenever()
            .post("http://localhost:$port/api/v1/orders")
            .then()
            .statusCode(201)

        RestAssured.given()
            .whenever()
            .get("/stats")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("totalEvents", greaterThan(initialTotalEvents))
    }

    @Test
    fun `should show unprocessed events after creating order`() {
        val createOrderDto = CreateOrderCommandRequest(
            customerId = 2L,
            totalAmount = BigDecimal("149.99"),
        )

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto)
            .whenever()
            .post("http://localhost:$port/api/v1/orders")
            .then()
            .statusCode(201)

        RestAssured.given()
            .whenever()
            .get("/unprocessed")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThanOrEqualTo(1))
    }

    @Test
    fun `should validate outbox event structure in unprocessed events`() {
        val createOrderDto = CreateOrderCommandRequest(
            customerId = 3L,
            totalAmount = BigDecimal("199.99"),
        )

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto)
            .whenever()
            .post("http://localhost:$port/api/v1/orders")
            .then()
            .statusCode(201)

        val response = RestAssured.given()
            .whenever()
            .get("/unprocessed")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .response()

        val events = response.jsonPath().getList<Map<String, Any>>("$")
        if (events.isNotEmpty()) {
            val firstEvent = events[0]

            assert(firstEvent.containsKey("id"))
            assert(firstEvent.containsKey("eventId"))
            assert(firstEvent.containsKey("eventType"))
            assert(firstEvent.containsKey("eventPayload"))
            assert(firstEvent.containsKey("routingKey"))
            assert(firstEvent.containsKey("exchange"))
            assert(firstEvent.containsKey("processed"))
            assert(firstEvent.containsKey("createdAt"))
            assert(firstEvent["processed"] == false)
        }
    }

    @Test
    fun `should validate all events structure`() {
        val createOrderDto = CreateOrderCommandRequest(
            customerId = 4L,
            totalAmount = BigDecimal("249.99"),
        )

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto)
            .whenever()
            .post("http://localhost:$port/api/v1/orders")
            .then()
            .statusCode(201)

        val response = RestAssured.given()
            .whenever()
            .get("")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .response()

        val events = response.jsonPath().getList<Map<String, Any>>("$")
        if (events.isNotEmpty()) {
            events.forEach { event ->
                assert(event.containsKey("id"))
                assert(event.containsKey("eventId"))
                assert(event.containsKey("eventType"))
                assert(event.containsKey("eventPayload"))
                assert(event.containsKey("routingKey"))
                assert(event.containsKey("exchange"))
                assert(event.containsKey("processed"))
                assert(event.containsKey("createdAt"))
            }
        }
    }

    @Test
    fun `should show processing rate calculation in stats`() {
        repeat(3) { index ->
            val createOrderDto = CreateOrderCommandRequest(
                customerId = (5 + index).toLong(),
                totalAmount = BigDecimal("${(index + 1) * 50}.99"),
            )

            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(createOrderDto)
                .whenever()
                .post("http://localhost:$port/api/v1/orders")
                .then()
                .statusCode(201)
        }

        val response = RestAssured.given()
            .whenever()
            .get("/stats")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .response()

        val totalEvents = response.path<Int>("totalEvents")
        val processedEvents = response.path<Int>("processedEvents")
        val unprocessedEvents = response.path<Int>("unprocessedEvents")
        val processingRate = response.path<String>("processingRate")

        assert(totalEvents == processedEvents + unprocessedEvents)
        assert(totalEvents >= 3)
        assert(processingRate.isNotEmpty())

        if (totalEvents > 0) {
            assert(processingRate.endsWith("%") || processingRate == "N/A")
        }
    }

    @Test
    fun `should handle concurrent outbox requests`() {
        val responses = (1..5).map { index ->
            Thread {
                RestAssured.given()
                    .whenever()
                    .get("/stats")
                    .then()
                    .statusCode(200)
            }.apply { start() }
        }

        responses.forEach { it.join() }

        RestAssured.given()
            .whenever()
            .get("/unprocessed")
            .then()
            .statusCode(200)

        RestAssured.given()
            .whenever()
            .get("")
            .then()
            .statusCode(200)
    }

    @Test
    fun `should validate event types in outbox events`() {
        val createOrderDto = CreateOrderCommandRequest(
            customerId = 10L,
            totalAmount = BigDecimal("999.99"),
        )

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto)
            .whenever()
            .post("http://localhost:$port/api/v1/orders")
            .then()
            .statusCode(201)

        val response = RestAssured.given()
            .whenever()
            .get("")
            .then()
            .statusCode(200)
            .extract()
            .response()

        val events = response.jsonPath().getList<Map<String, Any>>("$")
        val hasOrderEvent = events.any { event ->
            event["eventType"] == EventType.ORDER_EVENT.typeName
        }

        assert(hasOrderEvent) { "Should contain at least one ${EventType.ORDER_EVENT.typeName}" }
    }

    @Test
    fun `should maintain event ordering by creation time`() {
        val orderIds = mutableListOf<String>()
        repeat(3) { index ->
            val createOrderDto = CreateOrderCommandRequest(
                customerId = (20 + index).toLong(),
                totalAmount = BigDecimal("${(index + 1) * 100}.99"),
            )

            val response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(createOrderDto)
                .whenever()
                .post("http://localhost:$port/api/v1/orders")
                .then()
                .statusCode(201)
                .extract()
                .response()

            orderIds.add(response.path("id"))
        }

        val response = RestAssured.given()
            .whenever()
            .get("")
            .then()
            .statusCode(200)
            .extract()
            .response()

        val events = response.jsonPath().getList<Map<String, Any>>("$")
        if (events.size > 1) {
            for (i in 1 until events.size) {
                val prevCreatedAt = events[i - 1]["createdAt"] as String
                val currCreatedAt = events[i]["createdAt"] as String
                assert(prevCreatedAt <= currCreatedAt) { "Events should be ordered by creation time" }
            }
        }
    }
}
