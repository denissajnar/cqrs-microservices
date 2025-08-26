package dev.denissajnar.command.domain

import dev.denissajnar.shared.model.Status
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * MongoDB document representing an order in the command side of CQRS
 * This is the write model for order operations
 */
@Document(collection = "orders")
data class OrderCommand(
    @Id
    val id: UUID? = null,
    val customerId: Long,
    val totalAmount: BigDecimal,
    val status: Status = Status.PENDING,
    val createdAt: Instant = Instant.now()
)