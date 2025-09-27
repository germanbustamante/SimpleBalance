package com.germandebustamante.domain.datasource

import com.germandebustamante.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Suppress("TooManyFunctions")
interface LocalExpenseDataSource {
    fun getExpensesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>>
    fun getExpensesByDateRangeAndCategory(
        startDate: LocalDate, 
        endDate: LocalDate, 
        categoryCode: String
    ): Flow<List<Expense>>
    fun getTotalAmountByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<Long?>
    fun getExpenseById(id: Long): Flow<Expense?>
    fun getAllExpenses(limit: Int): Flow<List<Expense>>
    suspend fun insertExpense(expense: Expense): Long
    suspend fun insertExpenses(expenses: List<Expense>): List<Long>
    suspend fun updateExpense(expense: Expense): Int
    suspend fun deleteExpense(expense: Expense): Int
    suspend fun deleteExpenseById(id: Long): Int
    fun getExpenseCountByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<Int>
    fun getLastUsedCategoryCode(): Flow<String?>
}
