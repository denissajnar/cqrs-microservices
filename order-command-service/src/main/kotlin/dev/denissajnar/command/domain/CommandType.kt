package dev.denissajnar.command.domain

/**
 * Represents the type of command associated with an order.
 *
 * CommandType is used to define the specific action or operation being
 * performed on an order. It is part of the order command model and helps
 * track the lifecycle events of an order in the system.
 *
 * Commands include:
 * - CREATE: Indicates the creation of a new order.
 * - UPDATE: Represents an update to an existing order, such as modification of details.
 */
enum class CommandType {
    CREATE,
    UPDATE,
    DELETE,
}
