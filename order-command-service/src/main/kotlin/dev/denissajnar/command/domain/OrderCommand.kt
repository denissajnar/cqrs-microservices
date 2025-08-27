package dev.denissajnar.command.domain

import dev.denissajnar.shared.model.Status
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant

/**
 * MongoDB document representing a command/event in the order aggregate
 * This is immutable - each operation creates a new document
 */
@Document(collection = "order_commands")
data class OrderCommand(
    @Id
    val id: ObjectId = ObjectId.get(),
    val commandType: CommandType,
    val originalOrderId: ObjectId? = null,
    val customerId: Long,
    val totalAmount: BigDecimal,
    val status: Status = Status.PENDING,
    val createdAt: Instant = Instant.now(),
    val version: Long = 1L,
)
