package dev.denissajnar.command.exception

/**
 * Exception thrown when business validation rules are violated
 */
class BusinessValidationException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
