package dev.denissajnar.command.domain

import dev.denissajnar.shared.model.Status
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant

/**
 * Order Aggregate Root for Event Sourcing
 * Reconstructs state from events and provides business logic
 */
data class OrderAggregate(
    val id: ObjectId,
    val customerId: Long,
    val totalAmount: BigDecimal,
    val status: Status,
    val version: Long,
    val createdAt: Instant,
    val lastModifiedAt: Instant,
) {
    companion object {
        /**
         * Creates a new aggregate from a CREATE command
         */
        fun fromCreateCommand(command: OrderCommand): OrderAggregate {
            require(command.commandType == CommandType.CREATE) {
                "Cannot create aggregate from non-CREATE command"
            }

            return OrderAggregate(
                id = command.id,
                customerId = command.customerId,
                totalAmount = command.totalAmount,
                status = command.status,
                version = command.version,
                createdAt = command.createdAt,
                lastModifiedAt = command.createdAt,
            )
        }

        /**
         * Reconstructs aggregate state from a list of events in chronological order
         * This is the core of event sourcing - rebuilding state from events
         */
        fun fromEvents(events: List<OrderCommand>): OrderAggregate? {
            if (events.isEmpty()) return null

            val createEvent = events.first()
            require(createEvent.commandType == CommandType.CREATE) {
                "First event must be CREATE command"
            }

            var aggregate = fromCreateCommand(createEvent)
            events.drop(1).forEach { event ->
                aggregate = aggregate.applyEvent(event)
            }

            return aggregate
        }
    }

    /**
     * Applies an event to the current aggregate state
     * Returns a new aggregate with updated state
     */
    fun applyEvent(event: OrderCommand): OrderAggregate =
        when (event.commandType) {
            CommandType.CREATE -> throw IllegalArgumentException("Cannot apply CREATE event to existing aggregate")
            CommandType.UPDATE -> applyUpdateEvent(event)
            CommandType.DELETE -> applyDeleteEvent(event)
        }

    /**
     * Applies an UPDATE event to the aggregate
     */
    private fun applyUpdateEvent(event: OrderCommand): OrderAggregate =
        this.copy(
            customerId = event.customerId,
            totalAmount = event.totalAmount,
            status = event.status,
            version = event.version,
            lastModifiedAt = event.createdAt,
        )

    /**
     * Applies a DELETE event to the aggregate
     */
    private fun applyDeleteEvent(event: OrderCommand): OrderAggregate =
        this.copy(
            status = Status.CANCELLED,
            version = event.version,
            lastModifiedAt = event.createdAt,
        )

    /**
     * Checks if the aggregate is deleted/cancelled
     */
    fun isDeleted(): Boolean = status == Status.CANCELLED

    /**
     * Gets the current version for optimistic locking
     */
    fun getCurrentVersion(): Long = version
}
