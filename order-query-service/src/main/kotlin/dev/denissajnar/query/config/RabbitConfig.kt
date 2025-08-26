package dev.denissajnar.query.config

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * RabbitMQ configuration for the Order Query Service
 * Sets up messaging infrastructure for consuming domain events from command side
 */
@Configuration
class RabbitConfig {

    @Value("\${app.messaging.exchange:orders.exchange}")
    private lateinit var exchangeName: String

    @Value("\${app.messaging.queue:orders.query.queue}")
    private lateinit var queueName: String

    @Value("\${app.messaging.routing-key:order.created}")
    private lateinit var routingKey: String

    /**
     * Declares the exchange for order events (should already exist from command side)
     */
    @Bean
    fun ordersExchange(): TopicExchange = TopicExchange(exchangeName)

    /**
     * Declares the queue for order query service to consume events
     */
    @Bean
    fun ordersQueue(): Queue = QueueBuilder
        .durable(queueName)
        .build()

    /**
     * Binds the queue to the exchange with routing key
     */
    @Bean
    fun binding(): Binding = BindingBuilder
        .bind(ordersQueue())
        .to(ordersExchange())
        .with(routingKey)

    /**
     * Configures JSON message converter for RabbitMQ
     */
    @Bean
    fun jsonMessageConverter(): Jackson2JsonMessageConverter = Jackson2JsonMessageConverter()

    /**
     * Configures RabbitTemplate with JSON converter (for potential publishing if needed)
     */
    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate = RabbitTemplate(connectionFactory).apply {
        messageConverter = jsonMessageConverter()
    }
}