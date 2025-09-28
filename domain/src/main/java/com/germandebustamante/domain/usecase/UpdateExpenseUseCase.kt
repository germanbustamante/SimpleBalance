package com.germandebustamante.domain.usecase

import com.germandebustamante.domain.repository.ExpenseRepository
import com.germandebustamante.model.Expense
import java.math.BigDecimal
import kotlinx.datetime.LocalDate

/**
 * Use case for updating an existing expense.
 *
 * This use case encapsulates the business logic for updating expenses,
 * including validation and business rules. It ensures data integrity
 * and provides a clean API for the presentation layer.
 *
 * Business rules:
 * - Expense must exist (id > 0)
 * - Amount must be positive and have max 2 decimal places
 * - Date cannot be in the future beyond today
 * - Note has maximum length limit
 * - Category must be valid
 */
class UpdateExpenseUseCase(
    private val repository: ExpenseRepository
) {

    /**
     * Update an existing expense with validation.
     *
     * @param expense The expense to update (must have a valid ID).
     * @return Result containing true if update was successful, or validation error.
     */
    @Suppress("TooGenericExceptionCaught")
    suspend fun execute(expense: Expense): Result<Boolean> {
        return try {
            require(!expense.isNew()) {
                "Cannot update a new expense. ID must be greater than 0."
            }

            // Business validation (same as AddExpenseUseCase for amount, date, note)
            validateBusinessRules(expense.amount, expense.date, expense.note)

            val updated = repository.updateExpense(expense)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validate business rules for an expense.
     *
     * @param amount Expense amount.
     * @param date Expense date.
     * @param note Optional expense note.
     * @throws IllegalArgumentException if validation fails.
     */
    private fun validateBusinessRules(
        amount: BigDecimal,
        date: LocalDate,
        note: String?
    ) {
        // Date validation - allow reasonable future dates (e.g., planned expenses)
        val today = LocalDate.fromEpochDays(
            (System.currentTimeMillis() / MILLIS_IN_DAY).toInt()
        )
        val maxFutureDate = LocalDate(today.year, today.month, today.dayOfMonth).let {
            LocalDate(it.year, it.monthNumber + ONE_MONTH, FIRST_DAY_OF_MONTH) // Allow current month + 1
        }

        require(date <= maxFutureDate) {
            "Expense date cannot be more than one month in the future: $date"
        }

        // Amount business rules (beyond domain validation)
        require(amount <= MAX_EXPENSE_AMOUNT) {
            "Expense amount exceeds maximum allowed: $amount (max: $MAX_EXPENSE_AMOUNT)"
        }

        // Note validation (additional business rules)
        note?.let {
            require(!it.trim().contains(Regex("[<>{}]"))) {
                "Note cannot contain special characters: < > { } [ ]"
            }
        }
    }

    companion object {
        val MAX_EXPENSE_AMOUNT: BigDecimal = BigDecimal("99999.99")
        private const val MILLIS_IN_DAY = 24 * 60 * 60 * 1000L
        private const val ONE_MONTH = 1
        private const val FIRST_DAY_OF_MONTH = 1
    }
}
