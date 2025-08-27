package dev.denissajnar.query

import dev.denissajnar.query.config.TestcontainersConfiguration
import dev.denissajnar.query.repository.OrderQueryRepository
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
    lateinit var orderQueryRepository: OrderQueryRepository

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
        RestAssured.basePath = "/api/v1/orders"
    }

    @AfterEach
    fun tearDown() {
        orderQueryRepository.deleteAll()
    }
}
