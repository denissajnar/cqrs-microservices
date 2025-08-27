package dev.denissajnar.query.exception

import dev.denissajnar.shared.dto.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

/**
 * Global exception handler for the order query service
 * Provides centralized error handling and consistent error responses
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Handles database and data access exceptions
     */
    @ExceptionHandler(DataAccessException::class)
    fun handleDataAccessExceptions(
        ex: DataAccessException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "Database error occurred" }

        val response = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "DATABASE_ERROR",
            message = "Database query failed",
            path = request.requestURI,
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    /**
     * Handles method argument type mismatch (e.g., invalid Long format, invalid enum values)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatchExceptions(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "Type mismatch error: ${ex.message}" }

        val message = when {
            ex.requiredType?.isEnum == true -> "Invalid ${ex.name} value. Allowed values: ${
                ex.requiredType?.enumConstants?.joinToString(
                    ", ",
                )
            }"

            ex.name == "orderId" -> "Invalid order ID format. Expected Long format."
            ex.name == "customerId" -> "Invalid customer ID. Expected positive number."
            else -> "Invalid parameter format: ${ex.name}"
        }

        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "INVALID_PARAMETER",
            message = message,
            path = request.requestURI,
        )

        return ResponseEntity.badRequest().body(response)
    }

    /**
     * Handles illegal argument exceptions (e.g., negative customer IDs)
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentExceptions(
        ex: IllegalArgumentException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "Invalid argument: ${ex.message}" }

        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "INVALID_ARGUMENT",
            message = ex.message ?: "Invalid request parameter",
            path = request.requestURI,
        )

        return ResponseEntity.badRequest().body(response)
    }

    /**
     * Handles resource not found scenarios (though currently handled in controller)
     * This provides a fallback for any service-level not found exceptions
     */
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFoundExceptions(
        ex: NoSuchElementException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "Resource not found: ${ex.message}" }

        val response = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "RESOURCE_NOT_FOUND",
            message = ex.message ?: "Requested resource not found",
            path = request.requestURI,
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    /**
     * Handles all other unexpected exceptions
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericExceptions(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "Unexpected error occurred" }

        val response = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "INTERNAL_ERROR",
            message = "An unexpected error occurred",
            path = request.requestURI,
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}
