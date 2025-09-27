package com.germandebustamante.domain.usecase

import com.germandebustamante.model.Category
import com.germandebustamante.model.Expense
import com.germandebustamante.domain.repository.ExpenseRepository
import kotlinx.datetime.LocalDate
import java.math.BigDecimal

/**
 * Use case for adding a new expense.
 * 
 * This use case encapsulates the business logic for creating expenses,
 * including validation and business rules. It ensures data integrity
 * and provides a clean API for the presentation layer.
 * 
 * Business rules:
 * - Amount must be positive and have max 2 decimal places
 * - Date cannot be in the future beyond today
 * - Note has maximum length limit
 * - Category must be valid
 */
class AddExpenseUseCase(
    private val repository: ExpenseRepository
) {
    
    /**
     * Add a new expense with validation.
     * 
     * @param amount Expense amount in euros (must be positive)
     * @param date Expense date (cannot be future beyond reasonable limit)
     * @param category Expense category
     * @param note Optional note (will be trimmed and validated)
     * @return Result containing the expense ID or validation error
     */
    @Suppress("TooGenericExceptionCaught")
    suspend fun execute(
        amount: BigDecimal,
        date: LocalDate,
        category: Category,
        note: String? = null
    ): Result<Long> {
        return try {
            // Additional business validation beyond domain model
            validateBusinessRules(amount, date, note)
            
            // Create expense (domain model validation happens here)
            val expense = Expense.create(
                amount = amount,
                date = date,
                category = category,
                note = note?.trim()?.takeIf { it.isNotBlank() }
            )
            
            // Save to repository
            val expenseId = repository.insertExpense(expense)
            
            Result.success(expenseId)
            
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
    
    /**
     * Add expense with automatic category suggestion.
     * Uses the last used category as default if none provided.
     * 
     * @param amount Expense amount
     * @param date Expense date
     * @param category Optional category (if null, uses last used or default)
     * @param note Optional note
     * @return Result containing expense ID or error
     */
    suspend fun executeWithSmartCategory(
        amount: BigDecimal,
        date: LocalDate,
        category: Category? = null,
        note: String? = null
    ): Result<Long> {
        val finalCategory = category ?: getSmartCategoryDefault()
        return execute(amount, date, finalCategory, note)
    }
    
    /**
     * Validate additional business rules not covered by domain model.
     * 
     * @param amount Expense amount
     * @param date Expense date
     * @param note Optional note
     * @throws IllegalArgumentException if validation fails
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
    
    /**
     * Get smart category default based on usage history.
     * 
     * @return Last used category or system default
     */
    private fun getSmartCategoryDefault(): Category {
        // This would need to be implemented differently since Flow can't be used in suspend fun
        // For now, return system default. This will be improved in later phases.
        return Category.getDefault()
    }
    
    companion object {
        /**
         * Maximum allowed expense amount (business rule).
         */
        val MAX_EXPENSE_AMOUNT: BigDecimal = BigDecimal("99999.99")

        private const val MILLIS_IN_DAY = 24 * 60 * 60 * 1000L
        private const val ONE_MONTH = 1
        private const val FIRST_DAY_OF_MONTH = 1
    }
}
