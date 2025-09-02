package dev.denissajnar.command.exception

import dev.denissajnar.shared.dto.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.amqp.AmqpException
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.util.HtmlUtils

/**
 * Global exception handler for the order command service
 * Provides centralized error handling and consistent error responses
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val logger = KotlinLogging.logger {}

        /**
         * Sanitizes user input to prevent XSS attacks
         */
        private fun sanitizeInput(input: String?): String {
            return input?.let { HtmlUtils.htmlEscape(it) } ?: ""
        }
    }

    /**
     * Handles validation errors from @Valid annotation
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "Validation error: ${ex.message}" }

        val fieldErrors = ex.bindingResult.fieldErrors.map { fieldError ->
            "${sanitizeInput(fieldError.field)}: ${sanitizeInput(fieldError.defaultMessage)}"
        }

        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "VALIDATION_ERROR",
            message = "Validation failed for request",
            details = fieldErrors,
            path = sanitizeInput(request.requestURI),
        )

        return ResponseEntity.badRequest().body(response)
    }

    /**
     * Handles constraint violations from path variable validation
     */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationExceptions(
        ex: ConstraintViolationException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "Constraint violation error: ${ex.message}" }

        val violations = ex.constraintViolations.map { violation ->
            "${violation.propertyPath}: ${sanitizeInput(violation.message)}"
        }

        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "CONSTRAINT_VIOLATION",
            message = "Path variable validation failed",
            details = violations,
            path = sanitizeInput(request.requestURI),
        )

        return ResponseEntity.badRequest().body(response)
    }

    /**
     * Handles business logic validation errors
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBusinessLogicExceptions(
        ex: IllegalArgumentException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "Business logic error: ${ex.message}" }

        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "BUSINESS_ERROR",
            message = sanitizeInput(ex.message),
            path = sanitizeInput(request.requestURI),
        )

        return ResponseEntity.badRequest().body(response)
    }

    /**
     * Handles business validation exceptions
     */
    @ExceptionHandler(BusinessValidationException::class)
    fun handleBusinessValidationExceptions(
        ex: BusinessValidationException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "Business validation error: ${ex.message}" }

        val sanitizedMessage = sanitizeInput(ex.message)

        val isNotFoundError = sanitizedMessage.lowercase().contains("not found")
        val status = if (isNotFoundError) HttpStatus.NOT_FOUND else HttpStatus.BAD_REQUEST

        val response = ErrorResponse(
            status = status.value(),
            error = "BUSINESS_VALIDATION_ERROR",
            message = sanitizedMessage,
            path = sanitizeInput(request.requestURI),
        )

        return ResponseEntity.status(status).body(response)
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
            message = "Database operation failed",
            path = sanitizeInput(request.requestURI),
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    /**
     * Handles messaging/RabbitMQ exceptions
     */
    @ExceptionHandler(AmqpException::class)
    fun handleMessagingExceptions(
        ex: AmqpException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "Messaging error occurred" }

        val response = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "MESSAGING_ERROR",
            message = "Failed to publish event",
            path = sanitizeInput(request.requestURI),
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    /**
     * Handles malformed JSON or request body parsing errors
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadableExceptions(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "Invalid request body: ${ex.message}" }

        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "INVALID_REQUEST",
            message = "Invalid request body format",
            path = sanitizeInput(request.requestURI),
        )

        return ResponseEntity.badRequest().body(response)
    }

    /**
     * Handles method argument type mismatch (e.g., invalid Long format)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatchExceptions(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn { "Type mismatch error: ${ex.message}" }

        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "INVALID_PARAMETER",
            message = "Invalid parameter format: ${sanitizeInput(ex.name)}",
            path = sanitizeInput(request.requestURI),
        )

        return ResponseEntity.badRequest().body(response)
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
            path = sanitizeInput(request.requestURI),
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}
