package dev.denissajnar.command.repository

import dev.denissajnar.command.domain.CommandType
import dev.denissajnar.command.domain.OrderCommand
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

/**
 * MongoDB repository for OrderCommand entities
 * Provides CRUD operations and event sourcing queries for the command side of CQRS
 */
@Repository
interface OrderCommandRepository : MongoRepository<OrderCommand, ObjectId> {

    /**
     * Find all events for a specific aggregate (order) by original order ID or command ID
     * Used for event sourcing aggregate reconstruction
     */
    @Query($$"{$or: [{'originalOrderId': ?0}, {'_id': ?0}]}")
    fun findEventsByAggregateId(aggregateId: ObjectId): List<OrderCommand>

    /**
     * Find all events for a specific aggregate ordered by creation time
     * Essential for proper event sourcing replay
     */
    @Query(value = $$"{$or: [{'originalOrderId': ?0}, {'_id': ?0}]}", sort = "{createdAt: 1}")
    fun findEventsByAggregateIdOrderByCreatedAtAsc(aggregateId: ObjectId): List<OrderCommand>

    /**
     * Find the latest version of an aggregate by looking for the most recent event
     * Useful for optimistic locking and version checking
     */
    @Query(value = $$"{$or: [{'originalOrderId': ?0}, {'_id': ?0}]}", sort = "{createdAt: -1}")
    fun findLatestEventByAggregateId(aggregateId: ObjectId): OrderCommand?

    /**
     * Find the initial CREATE command for an aggregate
     * Useful for getting the original aggregate root
     */
    fun findByIdAndCommandType(id: ObjectId, commandType: CommandType): OrderCommand?
}
