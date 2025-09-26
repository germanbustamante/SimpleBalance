package com.germandebustamante.simplebalance.data.repository

import com.germandebustamante.simplebalance.data.local.database.ExpenseDao
import com.germandebustamante.simplebalance.data.local.mapper.toDomainModel
import com.germandebustamante.simplebalance.data.local.mapper.toDomainModels
import com.germandebustamante.simplebalance.data.local.mapper.toEntity
import com.germandebustamante.simplebalance.domain.model.Category
import com.germandebustamante.simplebalance.domain.model.Expense
import com.germandebustamante.simplebalance.domain.repository.ExpenseRepository
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
class ExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {
    
    override fun getExpensesByMonth(year: Int, month: Int): Flow<List<Expense>> {
        val (startEpochDay, endEpochDay) = getMonthRange(year, month)
        return expenseDao.getExpensesByDateRange(startEpochDay, endEpochDay)
            .map { entities -> entities.toDomainModels() }
    }
    
    override fun getExpensesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>> {
        val startEpochDay = startDate.toEpochDays().toLong()
        val endEpochDay = endDate.toEpochDays().toLong()
        return expenseDao.getExpensesByDateRange(startEpochDay, endEpochDay)
            .map { entities -> entities.toDomainModels() }
    }
    
    override fun getExpensesByMonthAndCategory(year: Int, month: Int, category: Category): Flow<List<Expense>> {
        val (startEpochDay, endEpochDay) = getMonthRange(year, month)
        return expenseDao.getExpensesByDateRangeAndCategory(startEpochDay, endEpochDay, category.code)
            .map { entities -> entities.toDomainModels() }
    }
    
    override fun getTotalByMonth(year: Int, month: Int): Flow<BigDecimal> {
        val (startEpochDay, endEpochDay) = getMonthRange(year, month)
        return expenseDao.getTotalAmountByDateRange(startEpochDay, endEpochDay)
            .map { totalCents ->
                // Convert cents to euros, handle null case (no expenses)
                totalCents?.let { BigDecimal(it).divide(BigDecimal(100)) } ?: BigDecimal.ZERO
            }
    }
    
    override fun getExpenseById(id: Long): Flow<Expense?> {
        return expenseDao.getExpenseById(id)
            .map { entity -> entity?.toDomainModel() }
    }
    
    override fun getLastUsedCategory(): Flow<Category?> {
        return expenseDao.getLastUsedCategoryCode()
            .map { categoryCode -> categoryCode?.let { Category.fromCode(it) } }
    }
    
    override suspend fun insertExpense(expense: Expense): Long {
        require(expense.isNew()) { 
            "Cannot insert expense with existing ID: ${expense.id}" 
        }
        
        // Expense validation is handled by the domain model's init block
        val entity = expense.toEntity()
        return expenseDao.insertExpense(entity)
    }
    
    override suspend fun updateExpense(expense: Expense): Boolean {
        require(!expense.isNew()) { 
            "Cannot update expense without valid ID: ${expense.id}" 
        }
        
        // Create updated expense with current timestamp
        val updatedExpense = expense.withUpdatedTimestamp()
        val entity = updatedExpense.toEntity()
        
        val rowsUpdated = expenseDao.updateExpense(entity)
        return rowsUpdated > 0
    }
    
    override suspend fun deleteExpense(expenseId: Long): Boolean {
        require(expenseId > 0) { 
            "Invalid expense ID for deletion: $expenseId" 
        }
        
        val rowsDeleted = expenseDao.deleteExpenseById(expenseId)
        return rowsDeleted > 0
    }
    
    override fun getExpenseCountByMonth(year: Int, month: Int): Flow<Int> {
        val (startEpochDay, endEpochDay) = getMonthRange(year, month)
        return expenseDao.getExpenseCountByDateRange(startEpochDay, endEpochDay)
    }
    
    override fun hasAnyExpenses(): Flow<Boolean> {
        return expenseDao.getAllExpenses(limit = 1)
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
        require(month in 1..12) { 
            "Month must be between 1 and 12, got: $month" 
        }
        
        val startOfMonth = LocalDate(year, month, 1)
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
    private fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    }
}