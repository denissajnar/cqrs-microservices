package dev.denissajnar.shared.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Instant

/**
 * Standard error response DTO for consistent error handling across services
 */
data class ErrorResponse(
    /**
     * HTTP status code (e.g., 400, 404, 500)
     */
    val status: Int,

    /**
     * Error type/category (e.g., VALIDATION_ERROR, BUSINESS_ERROR)
     */
    val error: String,

    /**
     * Human-readable error message
     */
    val message: String,

    /**
     * Detailed error information (optional)
     */
    val details: List<String>? = null,

    /**
     * Request path where error occurred
     */
    val path: String,

    /**
     * Timestamp when error occurred
     */
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val timestamp: Instant = Instant.now(),
)
