package dev.denissajnar.query.repository

import dev.denissajnar.query.entity.Order
import dev.denissajnar.shared.model.Status
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * JPA repository for Order entities in the query side of CQRS
 * Provides read operations for the query model
 */
@Repository
interface OrderRepository : CrudRepository<Order, UUID> {

    /**
     * Find orders by customer ID ordered by creation date
     * @param customerId the customer identifier
     * @return list of orders for the given customer ordered by created date descending
     */
    fun findByCustomerIdOrderByCreatedAtDesc(customerId: Long): List<Order>

    /**
     * Find orders by status
     * @param status the order status
     * @return list of orders with the given status
     */
    fun findByStatus(status: Status): List<Order>
}