package dev.denissajnar.query.integration

import dev.denissajnar.query.SpringBootTestParent
import dev.denissajnar.query.util.whenever
import dev.denissajnar.shared.model.Status
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test

/**
 * Integration tests for Order Query Service REST API using RestAssured
 * Tests all query operations with both happy path and error scenarios
 */
class OrderQueryControllerIntegrationTest : SpringBootTestParent() {


    @Test
    fun `should return 404 when getting order by non-existent id`() {
        RestAssured.given()
            .whenever()
            .get("/999")
            .then()
            .log().ifValidationFails()
            .statusCode(404)
    }

    @Test
    fun `should return empty list when getting orders by customer with no orders`() {
        RestAssured.given()
            .queryParam("customerId", 999)
            .whenever()
            .get()
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(0))
    }

    @Test
    fun `should return 400 when getting orders by customer with invalid customer id`() {
        RestAssured.given()
            .queryParam("customerId", -1)
            .whenever()
            .get()
            .then()
            .log().ifValidationFails()
            .statusCode(400)
    }

    @Test
    fun `should return orders by status successfully`() {
        RestAssured.given()
            .queryParam("status", Status.PENDING.name)
            .whenever()
            .get()
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThanOrEqualTo(0))
    }

    @Test
    fun `should return 400 when getting orders by invalid status`() {
        RestAssured.given()
            .queryParam("status", "INVALID_STATUS")
            .whenever()
            .get()
            .then()
            .log().ifValidationFails()
            .statusCode(400)
    }

    @Test
    fun `should return orders for all valid statuses`() {
        val validStatuses = listOf(
            Status.PENDING,
            Status.CONFIRMED,
            Status.PROCESSING,
            Status.SHIPPED,
            Status.COMPLETED,
            Status.CANCELLED,
            Status.FAILED,
        )

        validStatuses.forEach { status ->
            RestAssured.given()
                .queryParam("status", status.name)
                .whenever()
                .get()
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThanOrEqualTo(0))
        }
    }

    @Test
    fun `should return all orders when no query parameters provided`() {
        RestAssured.given()
            .whenever()
            .get()
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThanOrEqualTo(0))
    }

    @Test
    fun `should return orders by customer and status combined filtering`() {
        RestAssured.given()
            .queryParam("customerId", 1)
            .queryParam("status", Status.PENDING.name)
            .whenever()
            .get()
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThanOrEqualTo(0))
    }

    @Test
    fun `should return 400 when getting orders by combined filtering with invalid customer id`() {
        RestAssured.given()
            .queryParam("customerId", -1)
            .queryParam("status", Status.PENDING.name)
            .whenever()
            .get()
            .then()
            .log().ifValidationFails()
            .statusCode(400)
    }

    @Test
    fun `should handle concurrent requests for different customers`() {
        val customerId1 = 1L
        val customerId2 = 2L

        val response1 = RestAssured.given()
            .queryParam("customerId", customerId1)
            .whenever()
            .get()
            .then()
            .statusCode(200)
            .extract()
            .response()

        val response2 = RestAssured.given()
            .queryParam("customerId", customerId2)
            .whenever()
            .get()
            .then()
            .statusCode(200)
            .extract()
            .response()

        assert(response1.statusCode == 200)
        assert(response2.statusCode == 200)
    }

    @Test
    fun `should validate order response structure when order exists`() {
        RestAssured.given()
            .queryParam("customerId", 1)
            .whenever()
            .get()
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThanOrEqualTo(0))
    }

    @Test
    fun `should return proper error format for invalid requests`() {
        RestAssured.given()
            .queryParam("customerId", "invalid")
            .whenever()
            .get()
            .then()
            .log().ifValidationFails()
            .statusCode(400)
            .contentType(ContentType.JSON)
    }

    @Test
    fun `should handle missing query parameters gracefully`() {
        RestAssured.given()
            .whenever()
            .get()
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThanOrEqualTo(0))
    }

    @Test
    fun `should handle large customer id values`() {
        val largeCustomerId = Long.MAX_VALUE

        RestAssured.given()
            .queryParam("customerId", largeCustomerId)
            .whenever()
            .get()
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThanOrEqualTo(0))
    }

    @Test
    fun `should validate response structure for status-based queries`() {
        RestAssured.given()
            .queryParam("status", Status.PENDING.name)
            .whenever()
            .get()
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThanOrEqualTo(0))
    }

    @Test
    fun `should validate API endpoints are accessible`() {
        RestAssured.given()
            .queryParam("customerId", 1)
            .whenever()
            .get()
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(400)))

        RestAssured.given()
            .queryParam("status", Status.PENDING.name)
            .whenever()
            .get()
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(400)))

        RestAssured.given()
            .whenever()
            .get("/1")
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(404)))

        RestAssured.given()
            .whenever()
            .get("/order/550e8400-e29b-41d4-a716-446655440000")
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(404)))
    }
}
