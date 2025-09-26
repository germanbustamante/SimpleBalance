package com.germandebustamante.simplebalance.domain.usecase

import com.germandebustamante.simplebalance.domain.model.Category
import com.germandebustamante.simplebalance.domain.model.Expense
import com.germandebustamante.simplebalance.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case for retrieving monthly expenses with business logic.
 * 
 * This use case handles:
 * - Monthly expense retrieval with proper date validation
 * - Category filtering
 * - Business-level data transformations
 * - Sorting and grouping logic for UI consumption
 */
class GetMonthlyExpensesUseCase(
    private val repository: ExpenseRepository
) {
    
    /**
     * Get all expenses for a specific month.
     * 
     * @param year Year (e.g., 2024)
     * @param month Month (1-12)
     * @return Flow of expenses grouped and sorted for UI display
     */
    fun execute(year: Int, month: Int): Flow<List<Expense>> {
        validateMonthYear(year, month)
        return repository.getExpensesByMonth(year, month)
            .map { expenses -> expenses.sortedForDisplay() }
    }
    
    /**
     * Get expenses for a specific month and category.
     * 
     * @param year Year (e.g., 2024)
     * @param month Month (1-12)
     * @param category Category to filter by
     * @return Flow of filtered expenses
     */
    fun executeByCategory(year: Int, month: Int, category: Category): Flow<List<Expense>> {
        validateMonthYear(year, month)
        return repository.getExpensesByMonthAndCategory(year, month, category)
            .map { expenses -> expenses.sortedForDisplay() }
    }
    
    /**
     * Get expenses grouped by day for display.
     * 
     * @param year Year (e.g., 2024)
     * @param month Month (1-12)
     * @return Flow of map where key is day of month and value is list of expenses
     */
    fun executeGroupedByDay(year: Int, month: Int): Flow<Map<Int, List<Expense>>> {
        return execute(year, month)
            .map { expenses ->
                expenses.groupBy { it.date.dayOfMonth }
                    .toSortedMap(compareByDescending { it }) // Newest days first
            }
    }
    
    /**
     * Get expenses with statistics for the month.
     * 
     * @param year Year (e.g., 2024)
     * @param month Month (1-12)
     * @return Flow of monthly expenses with metadata
     */
    fun executeWithStats(year: Int, month: Int): Flow<MonthlyExpenseData> {
        return execute(year, month)
            .map { expenses ->
                MonthlyExpenseData(
                    expenses = expenses,
                    totalCount = expenses.size,
                    categoryCounts = expenses.groupBy { it.category }
                        .mapValues { (_, expenseList) -> expenseList.size },
                    averagePerDay = calculateAveragePerDay(expenses, year, month),
                    hasExpenses = expenses.isNotEmpty()
                )
            }
    }
    
    /**
     * Check if the specified month has any expenses.
     * 
     * @param year Year (e.g., 2024)
     * @param month Month (1-12)
     * @return Flow of boolean indicating if month has expenses
     */
    fun hasExpensesForMonth(year: Int, month: Int): Flow<Boolean> {
        return repository.getExpenseCountByMonth(year, month)
            .map { count -> count > 0 }
    }
    
    /**
     * Sort expenses for optimal display order.
     * 
     * @receiver List of expenses
     * @return Sorted list (newest first, then by update time)
     */
    private fun List<Expense>.sortedForDisplay(): List<Expense> {
        return sortedWith(
            compareByDescending<Expense> { it.date }
                .thenByDescending { it.updatedAt }
        )
    }
    
    /**
     * Calculate average expense amount per day for the month.
     * 
     * @param expenses List of expenses
     * @param year Year for day count calculation
     * @param month Month for day count calculation
     * @return Average amount per day
     */
    private fun calculateAveragePerDay(
        expenses: List<Expense>, 
        year: Int, 
        month: Int
    ): java.math.BigDecimal {
        if (expenses.isEmpty()) return java.math.BigDecimal.ZERO
        
        val totalAmount = expenses.sumOf { it.amount }
        val daysInMonth = getDaysInMonth(year, month)
        
        return totalAmount.divide(
            java.math.BigDecimal(daysInMonth),
            2, // 2 decimal places
            java.math.RoundingMode.HALF_UP
        )
    }
    
    /**
     * Get number of days in a month.
     * 
     * @param year Year (for leap year calculation)
     * @param month Month (1-12)
     * @return Number of days in the month
     */
    private fun getDaysInMonth(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> throw IllegalArgumentException("Invalid month: $month")
        }
    }
    
    /**
     * Check if a year is a leap year.
     * 
     * @param year Year to check
     * @return true if leap year
     */
    private fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    }
    
    /**
     * Validate month and year parameters.
     * 
     * @param year Year to validate
     * @param month Month to validate (1-12)
     * @throws IllegalArgumentException if parameters are invalid
     */
    private fun validateMonthYear(year: Int, month: Int) {
        require(month in 1..12) { 
            "Month must be between 1 and 12, got: $month" 
        }
        require(year in MIN_VALID_YEAR..MAX_VALID_YEAR) { 
            "Year must be between $MIN_VALID_YEAR and $MAX_VALID_YEAR, got: $year" 
        }
    }
    
    companion object {
        private const val MIN_VALID_YEAR = 2000
        private const val MAX_VALID_YEAR = 2100
    }
}

/**
 * Data class containing monthly expense information with statistics.
 * 
 * @param expenses List of expenses for the month
 * @param totalCount Total number of expenses
 * @param categoryCounts Count of expenses per category
 * @param averagePerDay Average expense amount per day
 * @param hasExpenses Whether the month has any expenses
 */
data class MonthlyExpenseData(
    val expenses: List<Expense>,
    val totalCount: Int,
    val categoryCounts: Map<Category, Int>,
    val averagePerDay: java.math.BigDecimal,
    val hasExpenses: Boolean
)