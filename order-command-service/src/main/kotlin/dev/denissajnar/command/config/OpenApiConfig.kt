package dev.denissajnar.command.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI configuration for the Order Command Service
 * Provides Swagger UI documentation for the command side API
 */
@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "Order Command Service API",
        version = "v1",
        description = "CQRS Command side for order management - handles write operations and publishes events"
    )
)
class OpenApiConfig {

    /**
     * Customizes the OpenAPI configuration
     * @return configured OpenAPI instance
     */
    @Bean
    fun customOpenAPI(): OpenAPI = OpenAPI()
        .servers(listOf(Server().url("http://localhost:8080").description("Development server")))
}