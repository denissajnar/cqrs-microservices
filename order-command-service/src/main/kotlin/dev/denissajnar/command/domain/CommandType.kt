package dev.denissajnar.command.domain

/**
 * Represents the type of command or event in the order aggregate.
 * It is used to specify the operation being performed on an aggregate.
 *
 * Types of commands:
 * - CREATE: Used for creating a new aggregate.
 * - UPDATE: Used for modifying an existing aggregate.
 * - DELETE: Used for marking an aggregate as deleted or cancelled.
 *
 * CommandType is primarily utilized in event sourcing to determine the
 * nature of state changes applied to an aggregate, and in MongoDB documents
 * to store the type of operation represented by the event.
 */
enum class CommandType {
    CREATE,
    UPDATE,
    DELETE,
}
