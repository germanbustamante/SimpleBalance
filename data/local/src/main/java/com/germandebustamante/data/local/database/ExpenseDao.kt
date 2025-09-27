package com.germandebustamante.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.germandebustamante.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for expense database operations.
 *
 * Provides reactive queries using Flow for automatic UI updates and optimized
 * queries with proper indexing for fast monthly expense retrieval.
 *
 * All operations are suspend functions for coroutine compatibility, except
 * query methods that return Flow for reactive programming.
 */
@Suppress("TooManyFunctions")
@Dao
interface ExpenseDao {

    /**
     * Get all expenses for a specific month range, ordered by date (newest first).
     * Uses indexed query on epoch_day for fast performance.
     *
     * @param startEpochDay First day of the month as epoch day
     * @param endEpochDay Last day of the month as epoch day
     * @return Flow of expenses for reactive UI updates
     */
    @Query(
        """
        SELECT * FROM expenses
        WHERE epoch_day BETWEEN :startEpochDay AND :endEpochDay 
        ORDER BY epoch_day DESC, updated_at_epoch_millis DESC
    """
    )
    fun getExpensesByDateRange(startEpochDay: Long, endEpochDay: Long): Flow<List<ExpenseEntity>>

    /**
     * Get all expenses for a specific month and category.
     * Uses composite index on (epoch_day, category_code) for optimal performance.
     *
     * @param startEpochDay First day of the month as epoch day
     * @param endEpochDay Last day of the month as epoch day
     * @param categoryCode Category code to filter by
     * @return Flow of filtered expenses
     */
    @Query(
        """
        SELECT * FROM expenses
        WHERE epoch_day BETWEEN :startEpochDay AND :endEpochDay 
        AND category_code = :categoryCode
        ORDER BY epoch_day DESC, updated_at_epoch_millis DESC
    """
    )
    fun getExpensesByDateRangeAndCategory(
        startEpochDay: Long,
        endEpochDay: Long,
        categoryCode: String,
    ): Flow<List<ExpenseEntity>>

    /**
     * Get total amount (in cents) for a specific month.
     * Returns null if no expenses found for the month.
     *
     * @param startEpochDay First day of the month as epoch day
     * @param endEpochDay Last day of the month as epoch day
     * @return Flow of total amount in cents, or null if no expenses
     */
    @Query(
        """
        SELECT SUM(amount_cents) FROM expenses 
        WHERE epoch_day BETWEEN :startEpochDay AND :endEpochDay
    """
    )
    fun getTotalAmountByDateRange(startEpochDay: Long, endEpochDay: Long): Flow<Long?>

    /**
     * Get expense by ID.
     *
     * @param id Expense ID
     * @return Flow of expense, or null if not found
     */
    @Query("SELECT * FROM expenses WHERE id = :id")
    fun getExpenseById(id: Long): Flow<ExpenseEntity?>

    /**
     * Get all expenses (for debugging/export purposes).
     * Limited to recent expenses to avoid memory issues.
     *
     * @param limit Maximum number of expenses to return
     * @return Flow of recent expenses
     */
    @Query(
        """
        SELECT * FROM expenses 
        ORDER BY updated_at_epoch_millis DESC 
        LIMIT :limit
    """
    )
    fun getAllExpenses(limit: Int = 1000): Flow<List<ExpenseEntity>>

    /**
     * Insert new expense.
     *
     * @param expense Expense entity to insert
     * @return Row ID of inserted expense
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    /**
     * Insert multiple expenses (for testing/import).
     *
     * @param expenses List of expenses to insert
     * @return List of row IDs
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>): List<Long>

    /**
     * Update existing expense.
     *
     * @param expense Expense entity with updated data
     * @return Number of rows updated (should be 1 for success)
     */
    @Update
    suspend fun updateExpense(expense: ExpenseEntity): Int

    /**
     * Delete expense.
     *
     * @param expense Expense entity to delete
     * @return Number of rows deleted (should be 1 for success)
     */
    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity): Int

    /**
     * Delete expense by ID.
     *
     * @param id Expense ID to delete
     * @return Number of rows deleted (should be 1 for success)
     */
    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long): Int

    /**
     * Get count of expenses in date range (for statistics).
     *
     * @param startEpochDay First day of the range
     * @param endEpochDay Last day of the range
     * @return Flow of expense count
     */
    @Query(
        """
        SELECT COUNT(*) FROM expenses 
        WHERE epoch_day BETWEEN :startEpochDay AND :endEpochDay
    """
    )
    fun getExpenseCountByDateRange(startEpochDay: Long, endEpochDay: Long): Flow<Int>

    /**
     * Get most recent category code for smart defaults.
     *
     * @return Flow of most recently used category code, or null if no expenses
     */
    @Query(
        """
        SELECT category_code FROM expenses 
        ORDER BY created_at_epoch_millis DESC 
        LIMIT 1
    """
    )
    fun getLastUsedCategoryCode(): Flow<String?>
}
