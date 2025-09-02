package dev.denissajnar.shared.model

import java.time.Instant

/**
 * Data class representing aggregate statistics for monitoring and debugging
 * Used in event sourcing to track aggregate metadata and lifecycle information
 */
data class AggregateStats(
    val aggregateId: String,
    val totalEvents: Int,
    val currentVersion: Long,
    val createdAt: Instant,
    val lastModifiedAt: Instant,
    val isDeleted: Boolean,
)
