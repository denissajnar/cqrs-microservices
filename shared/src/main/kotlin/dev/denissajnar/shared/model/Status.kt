package dev.denissajnar.shared.model

/**
 * Represents the various statuses an order can have in the system.
 *
 * The status transitions typically progress from PENDING to COMPLETED, with intermediate
 * statuses indicating the current state of the order. Certain statuses such as CANCELLED
 * or FAILED indicate a terminal state where the order cannot proceed further.
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
