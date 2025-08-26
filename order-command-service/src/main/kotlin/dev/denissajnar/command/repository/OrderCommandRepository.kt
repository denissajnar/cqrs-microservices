package dev.denissajnar.command.repository

import dev.denissajnar.command.domain.OrderCommand
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * MongoDB repository for OrderCommand entities
 * Provides basic CRUD operations for the command side of CQRS
 */
@Repository
interface OrderCommandRepository : MongoRepository<OrderCommand, UUID>