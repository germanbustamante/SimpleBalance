package com.germandebustamante.simplebalance.domain.usecase

import com.germandebustamante.simplebalance.domain.model.Category
import com.germandebustamante.simplebalance.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Use case for calculating monthly expense totals with business logic.
 * 
 * This use case handles:
 * - Monthly total calculations with proper rounding
 * - Category-specific totals
 * - Percentage breakdowns by category
 * - Comparison with other months
 * - Formatted output for UI display
 */
class GetMonthlyTotalUseCase(
    private val repository: ExpenseRepository
) {
    
    /**
     * Get total expenses for a specific month.
     * 
     * @param year Year (e.g., 2024)
     * @param month Month (1-12)
     * @return Flow of total amount rounded to 2 decimal places
     */
    fun execute(year: Int, month: Int): Flow<BigDecimal> {
        validateMonthYear(year, month)
        return repository.getTotalByMonth(year, month)
            .map { total -> total.setScale(2, RoundingMode.HALF_UP) }
    }
    
    /**
     * Get total with breakdown by category.
     * 
     * @param year Year (e.g., 2024)
     * @param month Month (1-12)
     * @return Flow of monthly total data with category breakdown
     */
    fun executeWithBreakdown(year: Int, month: Int): Flow<MonthlyTotalData> {
        validateMonthYear(year, month)
        
        val totalFlow = execute(year, month)
        val expensesFlow = repository.getExpensesByMonth(year, month)
        
        return combine(totalFlow, expensesFlow) { total, expenses ->
            val categoryTotals = expenses.groupBy { it.category }
                .mapValues { (_, expenseList) -> 
                    expenseList.sumOf { it.amount }.setScale(2, RoundingMode.HALF_UP)
                }
            
            val categoryPercentages = if (total > BigDecimal.ZERO) {
                categoryTotals.mapValues { (_, categoryTotal) ->
                    (categoryTotal.divide(total, 4, RoundingMode.HALF_UP) * BigDecimal(100))
                        .setScale(1, RoundingMode.HALF_UP)
                }
            } else {
                emptyMap()
            }
            
            MonthlyTotalData(
                total = total,
                categoryTotals = categoryTotals,
                categoryPercentages = categoryPercentages,
                expenseCount = expenses.size,
                averageExpenseAmount = calculateAverageExpenseAmount(expenses),
                hasExpenses = expenses.isNotEmpty()
            )
        }
    }
    
    /**
     * Get total for a specific category in a month.
     * 
     * @param year Year (e.g., 2024)
     * @param month Month (1-12)
     * @param category Category to calculate total for
     * @return Flow of category total
     */
    fun executeForCategory(year: Int, month: Int, category: Category): Flow<BigDecimal> {
        validateMonthYear(year, month)
        return repository.getExpensesByMonthAndCategory(year, month, category)
            .map { expenses -> 
                expenses.sumOf { it.amount }.setScale(2, RoundingMode.HALF_UP)
            }
    }
    
    /**
     * Compare current month total with previous month.
     * 
     * @param year Year (e.g., 2024)
     * @param month Month (1-12)
     * @return Flow of month comparison data
     */
    fun executeWithComparison(year: Int, month: Int): Flow<MonthComparisonData> {
        validateMonthYear(year, month)
        
        val currentTotal = execute(year, month)
        val (previousYear, previousMonth) = getPreviousMonth(year, month)
        val previousTotal = execute(previousYear, previousMonth)
        
        return combine(currentTotal, previousTotal) { current, previous ->
            val difference = current - previous
            val percentageChange = if (previous > BigDecimal.ZERO) {
                (difference.divide(previous, 4, RoundingMode.HALF_UP) * BigDecimal(100))
                    .setScale(1, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }
            
            MonthComparisonData(
                currentTotal = current,
                previousTotal = previous,
                difference = difference,
                percentageChange = percentageChange,
                isIncrease = difference > BigDecimal.ZERO,
                isDecrease = difference < BigDecimal.ZERO
            )
        }
    }
    
    /**
     * Get running total for the year up to specified month.
     * 
     * @param year Year (e.g., 2024)
     * @param month Month (1-12, inclusive)
     * @return Flow of year-to-date total
     */
    fun executeYearToDate(year: Int, month: Int): Flow<BigDecimal> {
        validateMonthYear(year, month)
        
        val monthTotals = (1..month).map { monthIndex ->
            execute(year, monthIndex)
        }
        
        return combine(monthTotals) { totals ->
            totals.fold(BigDecimal.ZERO) { sum, monthTotal -> 
                sum + monthTotal 
            }.setScale(2, RoundingMode.HALF_UP)
        }
    }
    
    /**
     * Calculate average expense amount for a list of expenses.
     * 
     * @param expenses List of expenses
     * @return Average amount per expense
     */
    private fun calculateAverageExpenseAmount(expenses: List<com.germandebustamante.simplebalance.domain.model.Expense>): BigDecimal {
        if (expenses.isEmpty()) return BigDecimal.ZERO
        
        val total = expenses.sumOf { it.amount }
        return total.divide(
            BigDecimal(expenses.size),
            2,
            RoundingMode.HALF_UP
        )
    }
    
    /**
     * Get previous month coordinates.
     * 
     * @param year Current year
     * @param month Current month
     * @return Pair of (previousYear, previousMonth)
     */
    private fun getPreviousMonth(year: Int, month: Int): Pair<Int, Int> {
        return if (month == 1) {
            Pair(year - 1, 12)
        } else {
            Pair(year, month - 1)
        }
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
 * Data class containing monthly total with detailed breakdown.
 * 
 * @param total Total amount for the month
 * @param categoryTotals Total amount per category
 * @param categoryPercentages Percentage of total per category
 * @param expenseCount Number of expenses in the month
 * @param averageExpenseAmount Average amount per expense
 * @param hasExpenses Whether the month has any expenses
 */
data class MonthlyTotalData(
    val total: BigDecimal,
    val categoryTotals: Map<Category, BigDecimal>,
    val categoryPercentages: Map<Category, BigDecimal>,
    val expenseCount: Int,
    val averageExpenseAmount: BigDecimal,
    val hasExpenses: Boolean
)

/**
 * Data class for comparing monthly totals.
 * 
 * @param currentTotal Current month total
 * @param previousTotal Previous month total
 * @param difference Difference between current and previous
 * @param percentageChange Percentage change from previous month
 * @param isIncrease Whether spending increased
 * @param isDecrease Whether spending decreased
 */
data class MonthComparisonData(
    val currentTotal: BigDecimal,
    val previousTotal: BigDecimal,
    val difference: BigDecimal,
    val percentageChange: BigDecimal,
    val isIncrease: Boolean,
    val isDecrease: Boolean
)