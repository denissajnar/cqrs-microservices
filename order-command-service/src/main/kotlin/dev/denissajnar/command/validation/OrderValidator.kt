package dev.denissajnar.command.validation

import dev.denissajnar.command.domain.OrderCommand
import dev.denissajnar.command.dto.CreateOrderCommandDTO
import dev.denissajnar.command.dto.UpdateOrderCommandDTO
import dev.denissajnar.command.exception.BusinessValidationException
import dev.denissajnar.shared.model.Status
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.math.BigDecimal

/**
 * Validator for order-related business rules and constraints
 */
@Component
class OrderValidator {

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val MAX_DECIMAL_PLACES = 2
    }

    /**
     * Validates order creation request
     */
    fun validateForCreation(dto: CreateOrderCommandDTO) {
        logger.debug { "Validating order creation for customer: ${dto.customerId}" }

        validateCommonBusinessRules(dto.customerId, dto.totalAmount)

        logger.debug { "Order creation validation passed for customer: ${dto.customerId}" }
    }

    /**
     * Validates order update request
     */
    fun validateForUpdate(dto: UpdateOrderCommandDTO, originalOrder: OrderCommand) {
        logger.debug { "Validating order update for order: ${originalOrder.id}" }

        validateCommonBusinessRules(dto.customerId, dto.totalAmount)
        validateOrderCanBeUpdated(originalOrder)

        logger.debug { "Order update validation passed for order: ${originalOrder.id}" }
    }

    /**
     * Validates order deletion request
     */
    fun validateForDeletion(originalOrder: OrderCommand) {
        logger.debug { "Validating order deletion for order: ${originalOrder.id}" }

        validateOrderCanBeDeleted(originalOrder)

        logger.debug { "Order deletion validation passed for order: ${originalOrder.id}" }
    }


    private fun validateCommonBusinessRules(customerId: Long?, totalAmount: BigDecimal?) {
        if (customerId != null && totalAmount != null) {
            when {
                customerId <= 0 -> throw BusinessValidationException("Customer ID must be positive")
                totalAmount <= BigDecimal.ZERO -> throw BusinessValidationException("Total amount must be positive")
                totalAmount.scale() > MAX_DECIMAL_PLACES -> throw BusinessValidationException("Total amount cannot have more than $MAX_DECIMAL_PLACES decimal places")
            }
        }
    }

    private fun validateOrderCanBeUpdated(originalOrder: OrderCommand) =
        when (originalOrder.status) {
            Status.SHIPPED,
            Status.COMPLETED,
            Status.CANCELLED,
            Status.FAILED,
                -> throw BusinessValidationException("Order cannot be updated in status: ${originalOrder.status}")

            else -> Unit
        }

    private fun validateOrderCanBeDeleted(originalOrder: OrderCommand) =
        when (originalOrder.status) {
            Status.SHIPPED,
            Status.COMPLETED,
                ->
                throw BusinessValidationException("Order cannot be deleted in status: ${originalOrder.status}")

            Status.PROCESSING ->
                throw BusinessValidationException("Cannot delete order that is being processed")

            else -> Unit
        }
}
