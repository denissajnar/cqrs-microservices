package dev.denissajnar.shared.model

/**
 * Enum representing the various statuses that an entity or process can have.
 *
 * The statuses are used to define the state or progress of a specific operation
 * or lifecycle. These values are commonly associated with workflows, order
 * processing, or event-based systems to determine and track the current state.
 */
enum class Status {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    COMPLETED,
    CANCELLED,
    FAILED
}
