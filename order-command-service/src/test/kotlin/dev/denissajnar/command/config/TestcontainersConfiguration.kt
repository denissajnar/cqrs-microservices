package dev.denissajnar.command.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.RabbitMQContainer

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun mongoDbContainer(): MongoDBContainer = MongoDBContainer("mongo:latest")

    @Bean
    @ServiceConnection
    fun rabbitContainer(): RabbitMQContainer = RabbitMQContainer("rabbitmq:latest")
}
