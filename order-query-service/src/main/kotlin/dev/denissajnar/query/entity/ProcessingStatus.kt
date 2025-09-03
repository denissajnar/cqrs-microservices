package dev.denissajnar.query.entity

/**
 * Processing status for inbox events
 */
enum class ProcessingStatus {
    PROCESSED,
    DEFERRED,
    FAILED,
    EXPIRED
}
