package com.germandebustamante.domain.usecase

import com.germandebustamante.model.Category
import com.germandebustamante.model.Expense
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import java.math.BigDecimal

/**
 * Use case for validating expense data.
 *
 * This use case encapsulates the business rules for expense validation,
 * ensuring consistency and data integrity across the application.
 */
class ValidateExpenseUseCase {

    /**
     * Validates the provided expense data against business rules.
     *
     * @param amount Expense amount.
     * @param date Expense date.
     * @param note Optional expense note.
     * @throws IllegalArgumentException if any validation rule is violated.
     */
    fun execute(
        amount: BigDecimal,
        date: LocalDate,
        note: String?
    ) {
        // Amount validation
        require(amount > BigDecimal.ZERO) {
            "Expense amount must be positive, got: $amount"
        }
        require(amount.scale() <= DECIMAL_PLACES) {
            "Expense amount cannot have more than $DECIMAL_PLACES decimal places, got: $amount"
        }
        require(amount <= MAX_EXPENSE_AMOUNT) {
            "Expense amount exceeds maximum allowed: $amount (max: $MAX_EXPENSE_AMOUNT)"
        }

        // Date validation - allow reasonable future dates (e.g., planned expenses)
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val maxFutureDate = today.plus(DatePeriod(months = ONE_MONTH))

        require(date <= maxFutureDate) {
            "Expense date cannot be more than one month in the future: $date"
        }

        // Note validation
        note?.let {
            val trimmedNote = it.trim()
            require(trimmedNote.length <= Expense.MAX_NOTE_LENGTH) {
                "Note cannot exceed ${Expense.MAX_NOTE_LENGTH} characters, got: ${trimmedNote.length}"
            }
            require(!trimmedNote.contains(Regex("[<>{}]"))) {
                "Note cannot contain special characters: < > { } [ ]"
            }
        }
    }

    companion object {
        val MAX_EXPENSE_AMOUNT: BigDecimal = BigDecimal("99999.99")
        private const val DECIMAL_PLACES = 2
        private const val ONE_MONTH = 1
    }
}
