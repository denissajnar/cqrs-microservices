package dev.denissajnar.command.integration

import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Integration tests for Order Command Service
 * Uses TestContainers for MongoDB and RabbitMQ
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderCommandIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    companion object {
        @Container
        val mongoDBContainer: MongoDBContainer = MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017)

        @Container
        val rabbitMQContainer: RabbitMQContainer = RabbitMQContainer("rabbitmq:3.12-management-alpine")
            .withExposedPorts(5672)

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri") { mongoDBContainer.replicaSetUrl }
            registry.add("spring.rabbitmq.host") { rabbitMQContainer.host }
            registry.add("spring.rabbitmq.port") { rabbitMQContainer.getMappedPort(5672) }
            registry.add("spring.rabbitmq.username") { rabbitMQContainer.adminUsername }
            registry.add("spring.rabbitmq.password") { rabbitMQContainer.adminPassword }
        }
    }

    @BeforeEach
    fun setup() {
        RestAssured.port = port
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    @Test
    fun `should create order successfully`() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "customerId": 1,
                    "totalAmount": 99.99
                }
            """
            )
            .`when`()
            .post("/api/v1/orders")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("customerId", equalTo(1))
            .body("totalAmount", equalTo(99.99f))
            .body("status", equalTo("PENDING"))
            .body("createdAt", notNullValue())
    }

    @Test
    fun `should return 400 for invalid request with negative customer ID`() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "customerId": -1,
                    "totalAmount": 99.99
                }
            """
            )
            .`when`()
            .post("/api/v1/orders")
            .then()
            .statusCode(400)
    }

    @Test
    fun `should return 400 for invalid request with zero amount`() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "customerId": 1,
                    "totalAmount": 0
                }
            """
            )
            .`when`()
            .post("/api/v1/orders")
            .then()
            .statusCode(400)
    }

    @Test
    fun `should return 400 for missing required fields`() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "customerId": 1
                }
            """
            )
            .`when`()
            .post("/api/v1/orders")
            .then()
            .statusCode(400)
    }
}