package dev.denissajnar.command.integration

import dev.denissajnar.command.SpringBootTestParent
import dev.denissajnar.command.dto.CreateOrderCommandDTO
import dev.denissajnar.command.util.whenever
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/**
 * Integration tests for Event Sourcing Controller REST API using RestAssured
 * Tests all event sourcing operations with both happy path and error scenarios
 */
class EventSourcingControllerIntegrationTest : SpringBootTestParent() {

    @BeforeEach
    override fun setUp() {
        super.setUp()
        RestAssured.basePath = "/api/v1/event-sourcing"
    }

    @Test
    fun `should return 400 when reconstructing aggregate with invalid ID`() {
        RestAssured.given()
            .whenever()
            .get("/aggregates/invalid-id/reconstruct")
            .then()
            .log().ifValidationFails()
            .statusCode(400)
    }

    @Test
    fun `should return 404 when reconstructing non-existent aggregate`() {
        val validButNonExistentId = "507f1f77bcf86cd799439011"

        RestAssured.given()
            .whenever()
            .get("/aggregates/$validButNonExistentId/reconstruct")
            .then()
            .log().ifValidationFails()
            .statusCode(404)
    }

    @Test
    fun `should reconstruct aggregate successfully for existing order`() {
        val createOrderDto = CreateOrderCommandDTO(
            customerId = 1L,
            totalAmount = BigDecimal("99.99"),
        )

        val createResponse = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto)
            .whenever()
            .post("http://localhost:$port/api/v1/orders")
            .then()
            .statusCode(201)
            .extract()
            .response()

        val orderId = createResponse.path<String>("id")

        RestAssured.given()
            .whenever()
            .get("/aggregates/$orderId/reconstruct")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("customerId", equalTo(1))
            .body("totalAmount", equalTo(99.99f))
            .body("status", equalTo("PENDING"))
            .body("version", equalTo(1))
    }

    @Test
    fun `should return event history for existing aggregate`() {
        val createOrderDto = CreateOrderCommandDTO(
            customerId = 2L,
            totalAmount = BigDecimal("149.99"),
        )

        val createResponse = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto)
            .whenever()
            .post("http://localhost:$port/api/v1/orders")
            .then()
            .statusCode(201)
            .extract()
            .response()

        val orderId = createResponse.path<String>("id")

        RestAssured.given()
            .whenever()
            .get("/aggregates/$orderId/events")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(1))
            .body("[0].commandType", equalTo("CREATE"))
            .body("[0].customerId", equalTo(2))
    }

    @Test
    fun `should return 400 when getting event history with invalid ID`() {
        RestAssured.given()
            .whenever()
            .get("/aggregates/invalid-id/events")
            .then()
            .log().ifValidationFails()
            .statusCode(400)
    }

    @Test
    fun `should return empty event history for non-existent aggregate`() {
        val validButNonExistentId = "507f1f77bcf86cd799439011"

        RestAssured.given()
            .whenever()
            .get("/aggregates/$validButNonExistentId/events")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(0))
    }

    @Test
    fun `should return aggregate stats for existing order`() {
        val createOrderDto = CreateOrderCommandDTO(
            customerId = 3L,
            totalAmount = BigDecimal("199.99"),
        )

        val createResponse = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto)
            .whenever()
            .post("http://localhost:$port/api/v1/orders")
            .then()
            .statusCode(201)
            .extract()
            .response()

        val orderId = createResponse.path<String>("id")

        RestAssured.given()
            .whenever()
            .get("/aggregates/$orderId/stats")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("aggregateId", equalTo(orderId))
            .body("totalEvents", equalTo(1))
            .body("currentVersion", equalTo(1))
            .body("deleted", equalTo(false))
            .body("createdAt", notNullValue())
            .body("lastModifiedAt", notNullValue())
    }

    @Test
    fun `should return 404 when getting stats for non-existent aggregate`() {
        val validButNonExistentId = "507f1f77bcf86cd799439011"

        RestAssured.given()
            .whenever()
            .get("/aggregates/$validButNonExistentId/stats")
            .then()
            .log().ifValidationFails()
            .statusCode(404)
    }

    @Test
    fun `should replay events successfully for existing aggregate`() {
        val createOrderDto = CreateOrderCommandDTO(
            customerId = 4L,
            totalAmount = BigDecimal("249.99"),
        )

        val createResponse = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto)
            .whenever()
            .post("http://localhost:$port/api/v1/orders")
            .then()
            .statusCode(201)
            .extract()
            .response()

        val orderId = createResponse.path<String>("id")

        RestAssured.given()
            .whenever()
            .post("/aggregates/$orderId/replay")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("customerId", equalTo(4))
            .body("totalAmount", equalTo(249.99f))
            .body("version", equalTo(1))
    }

    @Test
    fun `should return 404 when replaying events for non-existent aggregate`() {
        val validButNonExistentId = "507f1f77bcf86cd799439011"

        RestAssured.given()
            .whenever()
            .post("/aggregates/$validButNonExistentId/replay")
            .then()
            .log().ifValidationFails()
            .statusCode(404)
    }

    @Test
    fun `should validate aggregate exists for existing order`() {
        val createOrderDto = CreateOrderCommandDTO(
            customerId = 5L,
            totalAmount = BigDecimal("299.99"),
        )

        val createResponse = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto)
            .whenever()
            .post("http://localhost:$port/api/v1/orders")
            .then()
            .statusCode(201)
            .extract()
            .response()

        val orderId = createResponse.path<String>("id")

        RestAssured.given()
            .whenever()
            .get("/aggregates/$orderId/validate")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("exists", equalTo(true))
    }

    @Test
    fun `should validate aggregate does not exist for non-existent order`() {
        val validButNonExistentId = "507f1f77bcf86cd799439011"

        RestAssured.given()
            .whenever()
            .get("/aggregates/$validButNonExistentId/validate")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("exists", equalTo(false))
    }

    @Test
    fun `should get latest version for existing aggregate`() {
        val createOrderDto = CreateOrderCommandDTO(
            customerId = 6L,
            totalAmount = BigDecimal("349.99"),
        )

        val createResponse = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto)
            .whenever()
            .post("http://localhost:$port/api/v1/orders")
            .then()
            .statusCode(201)
            .extract()
            .response()

        val orderId = createResponse.path<String>("id")

        RestAssured.given()
            .whenever()
            .get("/aggregates/$orderId/version")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("version", equalTo(1))
    }

    @Test
    fun `should return 404 when getting version for non-existent aggregate`() {
        val validButNonExistentId = "507f1f77bcf86cd799439011"

        RestAssured.given()
            .whenever()
            .get("/aggregates/$validButNonExistentId/version")
            .then()
            .log().ifValidationFails()
            .statusCode(404)
    }

    @Test
    fun `should get current aggregate state for existing order`() {
        val createOrderDto = CreateOrderCommandDTO(
            customerId = 8L,
            totalAmount = BigDecimal("199.99"),
        )

        val createResponse = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto)
            .whenever()
            .post("http://localhost:$port/api/v1/orders")
            .then()
            .statusCode(201)
            .extract()
            .response()

        val orderId = createResponse.path<String>("id")

        RestAssured.given()
            .whenever()
            .get("/aggregates/$orderId/current-state")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id.timestamp", notNullValue())
            .body("customerId", equalTo(8))
            .body("totalAmount", equalTo(199.99f))
            .body("status", equalTo("PENDING"))
            .body("version", equalTo(1))
    }

    @Test
    fun `should return 400 when getting current state for non-existent aggregate`() {
        val validButNonExistentId = "507f1f77bcf86cd799439011"

        RestAssured.given()
            .whenever()
            .get("/aggregates/$validButNonExistentId/current-state")
            .then()
            .log().ifValidationFails()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("error", equalTo("BUSINESS_ERROR"))
            .body("message", equalTo("Aggregate not found: $validButNonExistentId"))
    }

    @Test
    fun `should return 400 when getting current state with invalid aggregate ID`() {
        RestAssured.given()
            .whenever()
            .get("/aggregates/invalid-id/current-state")
            .then()
            .log().ifValidationFails()
            .statusCode(400)
    }

    @Test
    fun `should handle multiple operations on same aggregate`() {
        val createOrderDto = CreateOrderCommandDTO(
            customerId = 7L,
            totalAmount = BigDecimal("399.99"),
        )

        val createResponse = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createOrderDto)
            .whenever()
            .post("http://localhost:$port/api/v1/orders")
            .then()
            .statusCode(201)
            .extract()
            .response()

        val orderId = createResponse.path<String>("id")

        RestAssured.given()
            .whenever()
            .get("/aggregates/$orderId/reconstruct")
            .then()
            .statusCode(200)

        RestAssured.given()
            .whenever()
            .get("/aggregates/$orderId/events")
            .then()
            .statusCode(200)
            .body("size()", equalTo(1))

        RestAssured.given()
            .whenever()
            .get("/aggregates/$orderId/stats")
            .then()
            .statusCode(200)
            .body("totalEvents", equalTo(1))

        RestAssured.given()
            .whenever()
            .post("/aggregates/$orderId/replay")
            .then()
            .statusCode(200)
    }
}
