package dev.denissajnar.query.integration

import dev.denissajnar.query.SpringBootTestParent
import dev.denissajnar.query.entity.InboxEvent
import dev.denissajnar.query.entity.ProcessingStatus
import dev.denissajnar.query.repository.InboxEventRepository
import dev.denissajnar.query.util.whenever
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.*

/**
 * Integration tests for Inbox Controller REST API using RestAssured
 * Tests all inbox monitoring operations with both happy path and error scenarios
 */
class InboxControllerIntegrationTest : SpringBootTestParent() {

    @Autowired
    lateinit var inboxEventRepository: InboxEventRepository

    @BeforeEach
    override fun setUp() {
        super.setUp()
        RestAssured.basePath = "/api/v1/inbox"
        inboxEventRepository.deleteAll()
    }

    @Test
    fun `should get events by processing status successfully`() {
        val processedEvent = createTestInboxEvent(ProcessingStatus.PROCESSED)
        val failedEvent = createTestInboxEvent(ProcessingStatus.FAILED)

        inboxEventRepository.saveAll(listOf(processedEvent, failedEvent))

        RestAssured.given()
            .whenever()
            .get("/status/PROCESSED")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(1))
            .body("[0].processingStatus", equalTo("PROCESSED"))

        RestAssured.given()
            .whenever()
            .get("/status/FAILED")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(1))
            .body("[0].processingStatus", equalTo("FAILED"))
    }

    @Test
    fun `should return 400 when getting events by invalid status`() {
        RestAssured.given()
            .whenever()
            .get("/status/INVALID_STATUS")
            .then()
            .log().ifValidationFails()
            .statusCode(400)
    }

    @Test
    fun `should get failed events successfully`() {
        val processedEvent = createTestInboxEvent(ProcessingStatus.PROCESSED)
        val failedEvent1 = createTestInboxEvent(ProcessingStatus.FAILED, "OrderCreatedEvent")
        val failedEvent2 = createTestInboxEvent(ProcessingStatus.FAILED, "OrderUpdatedEvent")

        inboxEventRepository.saveAll(listOf(processedEvent, failedEvent1, failedEvent2))

        RestAssured.given()
            .whenever()
            .get("/failed")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(2))
            .body("[0].processingStatus", equalTo("FAILED"))
            .body("[1].processingStatus", equalTo("FAILED"))
    }

    @Test
    fun `should get pending events successfully`() {
        val processedEvent = createTestInboxEvent(ProcessingStatus.PROCESSED)
        val failedEvent = createTestInboxEvent(ProcessingStatus.FAILED)
        val deferredEvent = createTestInboxEvent(ProcessingStatus.DEFERRED)
        val expiredEvent = createTestInboxEvent(ProcessingStatus.EXPIRED)

        inboxEventRepository.saveAll(listOf(processedEvent, failedEvent, deferredEvent, expiredEvent))

        RestAssured.given()
            .whenever()
            .get("/pending")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(3))
            .body("processingStatus", everyItem(not(equalTo("PROCESSED"))))
    }

    @Test
    fun `should get inbox statistics successfully`() {
        val processedEvent1 = createTestInboxEvent(ProcessingStatus.PROCESSED)
        val processedEvent2 = createTestInboxEvent(ProcessingStatus.PROCESSED)
        val failedEvent = createTestInboxEvent(ProcessingStatus.FAILED)
        val deferredEvent = createTestInboxEvent(ProcessingStatus.DEFERRED)
        val expiredEvent = createTestInboxEvent(ProcessingStatus.EXPIRED)

        inboxEventRepository.saveAll(
            listOf(
                processedEvent1, processedEvent2, failedEvent, deferredEvent, expiredEvent,
            ),
        )

        RestAssured.given()
            .whenever()
            .get("/stats")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("totalEvents", equalTo(5))
            .body("processedEvents", equalTo(2))
            .body("failedEvents", equalTo(1))
            .body("deferredEvents", equalTo(1))
            .body("expiredEvents", equalTo(1))
            .body("pendingEvents", equalTo(3))
            .body("successRate", notNullValue())
    }

    @Test
    fun `should calculate success rate correctly in stats`() {
        val events = mutableListOf<InboxEvent>()
        repeat(8) {
            events.add(createTestInboxEvent(ProcessingStatus.PROCESSED, "Event$it"))
        }
        repeat(2) {
            events.add(createTestInboxEvent(ProcessingStatus.FAILED, "FailedEvent$it"))
        }

        inboxEventRepository.saveAll(events)

        val response = RestAssured.given()
            .whenever()
            .get("/stats")
            .then()
            .statusCode(200)
            .extract()
            .response()

        val totalEvents = response.path<Int>("totalEvents")
        val processedEvents = response.path<Int>("processedEvents")
        val successRate = response.path<String>("successRate")

        assert(totalEvents == 10)
        assert(processedEvents == 8)
        assert(successRate == "80.0%")
    }

    @Test
    fun `should get all events successfully`() {
        val event1 = createTestInboxEvent(ProcessingStatus.PROCESSED, "OrderCreatedEvent")
        val event2 = createTestInboxEvent(ProcessingStatus.FAILED, "OrderUpdatedEvent")

        inboxEventRepository.saveAll(listOf(event1, event2))

        RestAssured.given()
            .whenever()
            .get("")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(2))
    }

    @Test
    fun `should get events by type successfully`() {
        val orderCreatedEvent1 = createTestInboxEvent(ProcessingStatus.PROCESSED, "OrderCreatedEvent")
        val orderCreatedEvent2 = createTestInboxEvent(ProcessingStatus.FAILED, "OrderCreatedEvent")
        val orderUpdatedEvent = createTestInboxEvent(ProcessingStatus.PROCESSED, "OrderUpdatedEvent")

        inboxEventRepository.saveAll(listOf(orderCreatedEvent1, orderCreatedEvent2, orderUpdatedEvent))

        RestAssured.given()
            .whenever()
            .get("/type/OrderCreatedEvent")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(2))
            .body("eventType", everyItem(equalTo("OrderCreatedEvent")))

        RestAssured.given()
            .whenever()
            .get("/type/OrderUpdatedEvent")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(1))
            .body("[0].eventType", equalTo("OrderUpdatedEvent"))
    }

    @Test
    fun `should handle case insensitive event type search`() {
        val orderCreatedEvent = createTestInboxEvent(ProcessingStatus.PROCESSED, "OrderCreatedEvent")
        inboxEventRepository.save(orderCreatedEvent)

        RestAssured.given()
            .whenever()
            .get("/type/ordercreatedevent")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(1))
            .body("[0].eventType", equalTo("OrderCreatedEvent"))
    }

    @Test
    fun `should return empty list for non-existent event type`() {
        val orderCreatedEvent = createTestInboxEvent(ProcessingStatus.PROCESSED, "OrderCreatedEvent")
        inboxEventRepository.save(orderCreatedEvent)

        RestAssured.given()
            .whenever()
            .get("/type/NonExistentEvent")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(0))
    }

    @Test
    fun `should validate inbox event structure`() {
        val testEvent = createTestInboxEvent(ProcessingStatus.PROCESSED, "OrderCreatedEvent")
        inboxEventRepository.save(testEvent)

        val response = RestAssured.given()
            .whenever()
            .get("")
            .then()
            .statusCode(200)
            .extract()
            .response()

        val events = response.jsonPath().getList<Map<String, Any>>("$")
        assert(events.isNotEmpty())

        val event = events[0]

        assert(event.containsKey("id"))
        assert(event.containsKey("eventId"))
        assert(event.containsKey("messageId"))
        assert(event.containsKey("eventType"))
        assert(event.containsKey("processingStatus"))
        assert(event.containsKey("processedAt"))
        assert(event.containsKey("createdAt"))
        assert(event.containsKey("eventPayload"))
    }

    @Test
    fun `should handle empty inbox gracefully`() {
        inboxEventRepository.deleteAll()

        RestAssured.given()
            .whenever()
            .get("/stats")
            .then()
            .statusCode(200)
            .body("totalEvents", equalTo(0))
            .body("successRate", equalTo("N/A"))

        RestAssured.given()
            .whenever()
            .get("")
            .then()
            .statusCode(200)
            .body("size()", equalTo(0))

        RestAssured.given()
            .whenever()
            .get("/failed")
            .then()
            .statusCode(200)
            .body("size()", equalTo(0))

        RestAssured.given()
            .whenever()
            .get("/pending")
            .then()
            .statusCode(200)
            .body("size()", equalTo(0))
    }

    @Test
    fun `should maintain event ordering by creation time`() {
        val event1 = createTestInboxEvent(ProcessingStatus.PROCESSED, "Event1")
        Thread.sleep(10)
        val event2 = createTestInboxEvent(ProcessingStatus.PROCESSED, "Event2")
        Thread.sleep(10)
        val event3 = createTestInboxEvent(ProcessingStatus.PROCESSED, "Event3")

        inboxEventRepository.saveAll(listOf(event3, event1, event2))

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

    @Test
    fun `should handle concurrent requests gracefully`() {
        val events = (1..5).map { createTestInboxEvent(ProcessingStatus.PROCESSED, "Event$it") }
        inboxEventRepository.saveAll(events)

        val threads = (1..10).map { index ->
            Thread {
                RestAssured.given()
                    .whenever()
                    .get("/stats")
                    .then()
                    .statusCode(200)
            }.apply { start() }
        }

        threads.forEach { it.join() }

        RestAssured.given()
            .whenever()
            .get("")
            .then()
            .statusCode(200)
            .body("size()", equalTo(5))
    }

    private fun createTestInboxEvent(
        status: ProcessingStatus,
        eventType: String = "TestEvent",
    ): InboxEvent {
        return InboxEvent(
            eventId = System.nanoTime(),
            messageId = UUID.randomUUID().toString(),
            eventType = eventType,
            processingStatus = status,
            processedAt = if (status == ProcessingStatus.PROCESSED) Instant.now() else null,
            createdAt = Instant.now(),
            errorMessage = if (status == ProcessingStatus.FAILED) "Test error message" else null,
            eventPayload = """{"test": "payload"}""",
        )
    }
}
