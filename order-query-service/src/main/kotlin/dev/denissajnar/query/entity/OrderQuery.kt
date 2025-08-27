package dev.denissajnar.query.entity

import dev.denissajnar.shared.model.Status
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

/**
 * JPA entity representing an order in the query side of CQRS
 * This is the read model for order queries
 */
@Entity
@Table(name = "orders")
class OrderQuery(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(length = 36)
    var historyId: String? = null,

    @Column(name = "customer_id", nullable = false)
    var customerId: Long = 0L,

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var status: Status = Status.PENDING,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
)
