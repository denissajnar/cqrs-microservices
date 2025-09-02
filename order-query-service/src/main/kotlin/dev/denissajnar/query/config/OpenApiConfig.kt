package dev.denissajnar.query.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI configuration for the OrderQuery Query Service
 * Provides Swagger UI documentation for the query side API
 */
@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "OrderQuery Query Service API",
        version = "v1",
        description = "CQRS Query side for order management - handles read operations and processes events from command side",
    ),
)
class OpenApiConfig {

    /**
     * Customizes the OpenAPI configuration
     * @return configured OpenAPI instance
     */
    @Bean
    fun customOpenAPI(): OpenAPI =
        OpenAPI()
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:8081")
                        .description("Development server"),
                ),
            )
}
