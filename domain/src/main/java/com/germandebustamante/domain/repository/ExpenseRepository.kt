package com.germandebustamante.domain.repository

import com.germandebustamante.model.Category
import com.germandebustamante.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import java.math.BigDecimal

/**
 * Repository interface for expense data operations.
 *
 * This interface defines the contract for expense data access in the domain layer,
 * following Clean Architecture principles. It abstracts away data storage implementation
 * details and provides a clean API for use cases.
 *
 * All query methods return Flow for reactive programming and automatic UI updates.
 * All mutation methods are suspend functions for coroutine compatibility.
 *
 * Future extensions:
 * - User-scoped operations (add userId parameter)
 * - Cloud sync operations
 * - Batch operations for performance
 */
@Suppress("TooManyFunctions")
interface ExpenseRepository {

    /**
     * Get all expenses for a specific month.
     *
     * @param year Year (e.g., 2024)
     * @param month Month (1-12)
     * @return Flow of expenses ordered by date (newest first)
     */
    fun getExpensesByMonth(year: Int, month: Int): Flow<List<Expense>>

    /**
     * Get expenses for a specific date range.
     *
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return Flow of expenses in the date range
     */
    fun getExpensesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>>

    /**
     * Get expenses for a specific month and category.
     *
     * @param year Year (e.g., 2024)
     * @param month Month (1-12)
     * @param category Category to filter by
     * @return Flow of filtered expenses
     */
    fun getExpensesByMonthAndCategory(year: Int, month: Int, category: Category): Flow<List<Expense>>

    /**
     * Get total amount for a specific month.
     *
     * @param year Year (e.g., 2024)
     * @param month Month (1-12)
     * @return Flow of total amount, or BigDecimal.ZERO if no expenses
     */
    fun getTotalByMonth(year: Int, month: Int): Flow<BigDecimal>

    /**
     * Get expense by ID.
     *
     * @param id Expense ID
     * @return Flow of expense, or null if not found
     */
    fun getExpenseById(id: Long): Flow<Expense?>

    /**
     * Get the most recently used category for smart defaults.
     *
     * @return Flow of last used category, or null if no expenses exist
     */
    fun getLastUsedCategory(): Flow<Category?>

    /**
     * Insert new expense.
     *
     * @param expense Expense to insert (id should be 0 for new expense)
     * @return ID of the inserted expense
     * @throws IllegalArgumentException if expense validation fails
     */
    suspend fun insertExpense(expense: Expense): Long

    /**
     * Update existing expense.
     *
     * @param expense Expense to update (must have valid id > 0)
     * @return true if update was successful
     * @throws IllegalArgumentException if expense validation fails or ID is invalid
     */
    suspend fun updateExpense(expense: Expense): Boolean

    /**
     * Delete expense by ID.
     *
     * @param expenseId ID of expense to delete
     * @return true if deletion was successful
     */
    suspend fun deleteExpense(expenseId: Long): Boolean

    /**
     * Get count of expenses for a specific month (for statistics).
     *
     * @param year Year (e.g., 2024)
     * @param month Month (1-12)
     * @return Flow of expense count
     */
    fun getExpenseCountByMonth(year: Int, month: Int): Flow<Int>

    /**
     * Check if any expenses exist (for onboarding/empty states).
     *
     * @return Flow of boolean indicating if any expenses exist
     */
    fun hasAnyExpenses(): Flow<Boolean>
}
