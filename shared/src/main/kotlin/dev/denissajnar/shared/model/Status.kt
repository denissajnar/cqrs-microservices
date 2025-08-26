package dev.denissajnar.shared.model

/**
 * Represents the possible states of an order.
 *
 * PENDING - The order is being processed or is awaiting completion.
 * COMPLETED - The order has been fully processed and finalized.
 */
enum class Status {
    PENDING,
    COMPLETED
}