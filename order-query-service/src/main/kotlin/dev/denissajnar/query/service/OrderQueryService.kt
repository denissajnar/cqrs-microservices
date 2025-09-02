package dev.denissajnar.query.service

import dev.denissajnar.query.dto.OrderQueryDTO
import dev.denissajnar.query.mapper.toDTO
import dev.denissajnar.query.mapper.toDTOs
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
    fun findOrderById(orderId: Long): OrderQueryDTO? =
        orderQueryRepository.findByIdOrNull(orderId)
            ?.let { order ->
                logger.debug { "Found order with ID: $orderId" }
                order.toDTO()
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
    fun getOrdersByCustomer(customerId: Long): List<OrderQueryDTO> =
        orderQueryRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
            .toDTOs()
            .also { orders ->
                logger.debug { "Found ${orders.size} orders for customer: $customerId" }
            }

    /**
     * Retrieves orders by status
     * @param status the order status
     * @return list of order DTOs with the given status
     */
    fun getOrdersByStatus(status: Status): List<OrderQueryDTO> =
        orderQueryRepository.findByStatus(status)
            .toDTOs()
            .also { orders ->
                logger.debug { "Found ${orders.size} orders with status: $status" }
            }

    /**
     * Retrieves orders by customer ID and status
     * @param customerId the customer identifier
     * @param status the order status
     * @return list of order DTOs for the given customer and status
     */
    fun getOrdersByCustomerAndStatus(customerId: Long, status: Status): List<OrderQueryDTO> =
        orderQueryRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status)
            .toDTOs()
            .also { orders ->
                logger.debug { "Found ${orders.size} orders for customer: $customerId" }
            }

    /**
     * Retrieves an order by its history ID
     * @param historyId the history identifier from command side
     * @return the order DTO or null if not found
     */
    fun findOrderByHistoryId(historyId: String): OrderQueryDTO? =
        orderQueryRepository.findByHistoryId(historyId)
            ?.let { order ->
                logger.debug { "Found order with history ID: $historyId" }
                order.toDTO()
            }
            .also { result ->
                if (result == null) {
                    logger.debug { "OrderQuery not found with history ID: $historyId" }
                }
            }

    /**
     * Retrieves all orders
     * @return list of all order DTOs
     */
    fun getAllOrders(): List<OrderQueryDTO> =
        orderQueryRepository.findAll()
            .toList()
            .toDTOs()
            .also { orders ->
                logger.debug { "Found ${orders.size} total orders" }
            }
}
