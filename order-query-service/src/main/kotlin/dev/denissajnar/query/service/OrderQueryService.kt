package dev.denissajnar.query.service

import dev.denissajnar.query.dto.OrderQueryResponse
import dev.denissajnar.query.mapper.toResponse
import dev.denissajnar.query.mapper.toResponses
import dev.denissajnar.query.repository.OrderQueryRepository
import dev.denissajnar.shared.model.Status
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service for handling order queries in CQRS architecture
 * Responsible for read operations from the query side database
 */
@Service
@Transactional(readOnly = true)
class OrderQueryService(
    private val orderQueryRepository: OrderQueryRepository,
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Retrieves an order by its ID
     * @param orderId the order identifier
     * @return the order DTO or null if not found
     */
    fun findOrderById(orderId: Long): OrderQueryResponse? =
        orderQueryRepository.findByIdOrNull(orderId)
            ?.let { order ->
                logger.debug { "Found order with ID: $orderId" }
                order.toResponse()
            }
            .also { result ->
                if (result == null) {
                    logger.debug { "OrderQuery not found with ID: $orderId" }
                }
            }

    /**
     * Retrieves all orders for a specific customer
     * @param customerId the customer identifier
     * @return list of order DTOs for the customer
     */
    fun getOrdersByCustomer(customerId: Long): List<OrderQueryResponse> =
        orderQueryRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
            .toResponses()
            .also { orders ->
                logger.debug { "Found ${orders.size} orders for customer: $customerId" }
            }

    /**
     * Retrieves orders by status
     * @param status the order status
     * @return list of order DTOs with the given status
     */
    fun getOrdersByStatus(status: Status): List<OrderQueryResponse> =
        orderQueryRepository.findByStatus(status)
            .toResponses()
            .also { orders ->
                logger.debug { "Found ${orders.size} orders with status: $status" }
            }

    /**
     * Retrieves orders by customer ID and status
     * @param customerId the customer identifier
     * @param status the order status
     * @return list of order DTOs for the given customer and status
     */
    fun getOrdersByCustomerAndStatus(customerId: Long, status: Status): List<OrderQueryResponse> =
        orderQueryRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status)
            .toResponses()
            .also { orders ->
                logger.debug { "Found ${orders.size} orders for customer: $customerId" }
            }

    /**
     * Retrieves an order by its order ID
     * @param orderId the order identifier from command side
     * @return the order DTO or null if not found
     */
    fun findOrderByOrderId(orderId: String): OrderQueryResponse? =
        orderQueryRepository.findByOrderId(orderId)
            ?.let { order ->
                logger.debug { "Found order with order ID: $orderId" }
                order.toResponse()
            }
            .also { result ->
                if (result == null) {
                    logger.debug { "OrderQuery not found with order ID: $orderId" }
                }
            }

    /**
     * Retrieves all orders
     * @return list of all order DTOs
     */
    fun getAllOrders(): List<OrderQueryResponse> =
        orderQueryRepository.findAll()
            .toList()
            .toResponses()
            .also { orders ->
                logger.debug { "Found ${orders.size} total orders" }
            }
}
