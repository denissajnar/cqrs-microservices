package dev.denissajnar.command

import dev.denissajnar.command.config.TestcontainersConfiguration
import dev.denissajnar.command.repository.OrderCommandRepository
import dev.denissajnar.command.repository.OutboxEventRepository
import io.restassured.RestAssured
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.test.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration::class)
@ActiveProfiles("test")
abstract class SpringBootTestParent {

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    lateinit var orderCommandRepository: OrderCommandRepository

    @Autowired
    lateinit var outboxEventRepository: OutboxEventRepository

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
        RestAssured.basePath = "/api/v1/orders"
    }

    @AfterEach
    fun tearDown() {
        orderCommandRepository.deleteAll()
        outboxEventRepository.deleteAll()
    }
}
