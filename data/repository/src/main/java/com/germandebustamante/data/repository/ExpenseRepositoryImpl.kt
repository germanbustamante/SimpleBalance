package com.germandebustamante.data.repository

import com.germandebustamante.data.local.database.ExpenseDao
import com.germandebustamante.domain.datasource.LocalExpenseDataSource
import com.germandebustamante.domain.repository.ExpenseRepository
import com.germandebustamante.model.Category
import com.germandebustamante.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import java.math.BigDecimal

/**
 * Implementation of [ExpenseRepository] using Room database.
 *
 * This implementation:
 * - Uses [ExpenseDao] for database operations
 * - Handles entity ↔ domain model mapping
 * - Provides reactive data with Flow
 * - Implements proper error handling and validation
 * - Optimizes queries using indexed database columns
 *
 * All database operations run on the IO dispatcher via Room's built-in
 * thread management for suspend functions and Flow operations.
 */
@Suppress("TooManyFunctions")
class ExpenseRepositoryImpl(
    private val localExpenseDataSource: LocalExpenseDataSource,
) : ExpenseRepository {

    override fun getExpensesByMonth(year: Int, month: Int): Flow<List<Expense>> {
        val (startEpochDay, endEpochDay) = getMonthRange(year, month)
        return localExpenseDataSource.getExpensesByDateRange(
            LocalDate.fromEpochDays(startEpochDay.toInt()),
            LocalDate.fromEpochDays(endEpochDay.toInt())
        ).map { entities -> entities }
    }

    override fun getExpensesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>> {
        return localExpenseDataSource.getExpensesByDateRange(startDate, endDate)
            .map { entities -> entities }
    }

    override fun getExpensesByMonthAndCategory(year: Int, month: Int, category: Category): Flow<List<Expense>> {
        val (startEpochDay, endEpochDay) = getMonthRange(year, month)
        return localExpenseDataSource.getExpensesByDateRangeAndCategory(
            LocalDate.fromEpochDays(startEpochDay.toInt()),
            LocalDate.fromEpochDays(endEpochDay.toInt()),
            category.code
        ).map { entities -> entities }
    }

    override fun getTotalByMonth(year: Int, month: Int): Flow<BigDecimal> {
        val (startEpochDay, endEpochDay) = getMonthRange(year, month)
        return localExpenseDataSource.getTotalAmountByDateRange(
            LocalDate.fromEpochDays(startEpochDay.toInt()),
            LocalDate.fromEpochDays(endEpochDay.toInt())
        ).map { totalCents ->
            // Convert cents to euros, handle null case (no expenses)
            totalCents?.let { BigDecimal(it).divide(CENTS_TO_EUROS_DIVISOR) } ?: BigDecimal.ZERO
        }
    }

    override fun getExpenseById(id: Long): Flow<Expense?> {
        return localExpenseDataSource.getExpenseById(id)
            .map { entity -> entity }
    }

    override fun getLastUsedCategory(): Flow<Category?> {
        return localExpenseDataSource.getLastUsedCategoryCode()
            .map { categoryCode -> categoryCode?.let { Category.fromCode(it) } }
    }

    override suspend fun insertExpense(expense: Expense): Long {
        require(expense.isNew()) {
            "Cannot insert expense with existing ID: ${expense.id}"
        }

        // Expense validation is handled by the domain model's init block
        return localExpenseDataSource.insertExpense(expense)
    }

    override suspend fun updateExpense(expense: Expense): Boolean {
        require(!expense.isNew()) {
            "Cannot update expense without valid ID: ${expense.id}"
        }

        // Create updated expense with current timestamp
        val updatedExpense = expense.withUpdatedTimestamp()
        val rowsUpdated = localExpenseDataSource.updateExpense(updatedExpense)
        return rowsUpdated > NO_ROWS_UPDATED
    }

    override suspend fun deleteExpense(expenseId: Long): Boolean {
        require(expenseId > INVALID_ID) {
            "Invalid expense ID for deletion: $expenseId"
        }
        val rowsDeleted = localExpenseDataSource.deleteExpenseById(expenseId)
        return rowsDeleted > NO_ROWS_DELETED
    }

    override fun getExpenseCountByMonth(year: Int, month: Int): Flow<Int> {
        val (startEpochDay, endEpochDay) = getMonthRange(year, month)
        return localExpenseDataSource.getExpenseCountByDateRange(
            LocalDate.fromEpochDays(startEpochDay.toInt()),
            LocalDate.fromEpochDays(endEpochDay.toInt())
        )
    }

    override fun hasAnyExpenses(): Flow<Boolean> {
        return localExpenseDataSource.getAllExpenses(limit = SINGLE_EXPENSE_LIMIT)
            .map { expenses -> expenses.isNotEmpty() }
    }

    /**
     * Calculate epoch day range for a given month.
     *
     * @param year Year (e.g., 2024)
     * @param month Month (1-12)
     * @return Pair of (startEpochDay, endEpochDay) for the month
     */
    private fun getMonthRange(year: Int, month: Int): Pair<Long, Long> {
        require(month in MIN_MONTH_VALUE..MAX_MONTH_VALUE) {
            "Month must be between $MIN_MONTH_VALUE and $MAX_MONTH_VALUE, got: $month"
        }

        val startOfMonth = LocalDate(year, month, FIRST_DAY_OF_MONTH)
        val endOfMonth = LocalDate(year, month, startOfMonth.month.length(isLeapYear(year)))

        return Pair(
            startOfMonth.toEpochDays().toLong(),
            endOfMonth.toEpochDays().toLong()
        )
    }

    /**
     * Check if a year is a leap year.
     *
     * @param year Year to check
     * @return true if the year is a leap year
     */
    private fun isLeapYear(year: Int): Boolean =
        year % LEAP_YEAR_DIVISOR_4 == 0 && (year % LEAP_YEAR_DIVISOR_100 != 0 || year % LEAP_YEAR_DIVISOR_400 == 0)

    companion object {
        private val CENTS_TO_EUROS_DIVISOR = BigDecimal(100)
        private const val NO_ROWS_UPDATED = 0
        private const val INVALID_ID = 0L
        private const val NO_ROWS_DELETED = 0
        private const val SINGLE_EXPENSE_LIMIT = 1
        private const val MIN_MONTH_VALUE = 1
        private const val MAX_MONTH_VALUE = 12
        private const val FIRST_DAY_OF_MONTH = 1
        private const val LEAP_YEAR_DIVISOR_4 = 4
        private const val LEAP_YEAR_DIVISOR_100 = 100
        private const val LEAP_YEAR_DIVISOR_400 = 400
    }
}
