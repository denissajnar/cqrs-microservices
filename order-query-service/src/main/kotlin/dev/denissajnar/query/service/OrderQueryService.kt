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
        this@OrderQueryService.orderQueryRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
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
}
