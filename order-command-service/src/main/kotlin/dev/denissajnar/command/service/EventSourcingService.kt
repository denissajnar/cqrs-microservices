package dev.denissajnar.command.service

import dev.denissajnar.command.domain.OrderAggregate
import dev.denissajnar.command.domain.OrderCommand
import dev.denissajnar.command.repository.OrderCommandRepository
import dev.denissajnar.shared.model.AggregateStats
import io.github.oshai.kotlinlogging.KotlinLogging
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

/**
 * Service for Event Sourcing operations
 * Handles aggregate reconstruction from events and event replay
 */
@Service
class EventSourcingService(
    private val orderCommandRepository: OrderCommandRepository,
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Reconstructs an aggregate from its event history
     * Returns null if no events found for the aggregate
     */
    fun reconstructAggregate(aggregateId: ObjectId): OrderAggregate? {
        val events = orderCommandRepository.findEventsByAggregateIdOrderByCreatedAtAsc(aggregateId)

        if (events.isEmpty()) {
            logger.debug { "No events found for aggregate ID: $aggregateId" }
            return null
        }

        logger.debug { "Reconstructing aggregate from ${events.size} events for ID: $aggregateId" }
        val aggregate = OrderAggregate.fromEvents(events)

        logger.info { "Successfully reconstructed aggregate ID: $aggregateId, version: ${aggregate?.getCurrentVersion()}" }

        return aggregate
    }


    /**
     * Gets the current state of an aggregate by reconstructing from events
     * Throws exception if aggregate doesn't exist
     */
    fun getCurrentAggregateState(aggregateId: ObjectId): OrderAggregate =
        reconstructAggregate(aggregateId)
            ?: throw IllegalArgumentException("Aggregate not found: $aggregateId")

    /**
     * Gets the latest version number for an aggregate
     * Used for optimistic locking
     */
    fun getLatestVersion(aggregateId: ObjectId): Long? =
        orderCommandRepository.findLatestEventByAggregateId(aggregateId)?.version


    /**
     * Replays all events for an aggregate and returns the final state
     * Useful for debugging and state verification
     */
    fun replayEvents(aggregateId: ObjectId): OrderAggregate? {
        logger.info { "Starting event replay for aggregate ID: $aggregateId" }

        val events = orderCommandRepository.findEventsByAggregateIdOrderByCreatedAtAsc(aggregateId)
        if (events.isEmpty()) {
            logger.info { "No events to replay for aggregate ID: $aggregateId" }

            return null
        }

        logger.info { "Replaying ${events.size} events for aggregate ID: $aggregateId" }

        events.forEachIndexed { index, event ->
            logger.debug { "Replaying event ${index + 1}/${events.size}: ${event.commandType} at ${event.createdAt}" }
        }

        val finalState = OrderAggregate.fromEvents(events)
        logger.info { "Event replay completed for aggregate ID: $aggregateId, final version: ${finalState?.getCurrentVersion()}" }

        return finalState
    }

    /**
     * Gets all events for an aggregate in chronological order
     * Useful for debugging and auditing
     */
    fun getEventHistory(aggregateId: ObjectId): List<OrderCommand> {
        return orderCommandRepository.findEventsByAggregateIdOrderByCreatedAtAsc(aggregateId)
    }

    /**
     * Validates that an aggregate exists and is not deleted
     */
    fun validateAggregateExists(aggregateId: ObjectId): Boolean {
        val aggregate = reconstructAggregate(aggregateId)

        return aggregate != null && !aggregate.isDeleted()
    }

    /**
     * Gets aggregate statistics for monitoring
     */
    fun getAggregateStats(aggregateId: ObjectId): AggregateStats? {
        val events = getEventHistory(aggregateId)

        if (events.isEmpty()) return null

        val aggregate = OrderAggregate.fromEvents(events)

        return aggregate?.let { agg ->
            AggregateStats(
                aggregateId = aggregateId.toHexString(),
                totalEvents = events.size,
                currentVersion = agg.getCurrentVersion(),
                createdAt = agg.createdAt,
                lastModifiedAt = agg.lastModifiedAt,
                isDeleted = agg.isDeleted(),
            )
        }
    }
}


