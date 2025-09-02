package dev.denissajnar.query.repository

import dev.denissajnar.query.entity.OrderQuery
import dev.denissajnar.shared.model.Status
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

/**
 * JPA repository for OrderQuery entities in the query side of CQRS
 * Provides read operations for the query model
 */
@Repository
interface OrderQueryRepository : CrudRepository<OrderQuery, Long> {

    /**
     * Find orders by customer ID ordered by creation date
     * @param customerId the customer identifier
     * @return list of orders for the given customer ordered by created date descending
     */
    fun findByCustomerIdOrderByCreatedAtDesc(customerId: Long): List<OrderQuery>

    /**
     * Find orders by customer ID and status ordered by creation date
     * @param customerId the customer identifier
     * @param status the order status
     * @return list of orders for the given customer and status ordered by created date descending
     */
    fun findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId: Long, status: Status): List<OrderQuery>

    /**
     * Find orders by status
     * @param status the order status
     * @return list of orders with the given status
     */
    fun findByStatus(status: Status): List<OrderQuery>

    /**
     * Find order by history ID (from command side events)
     * @param historyId the history identifier from command side
     * @return the order with the given history ID or null if not found
     */
    fun findByHistoryId(historyId: String): OrderQuery?
}
