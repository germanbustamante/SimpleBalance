package com.germandebustamante.data.local.datasource

import com.germandebustamante.data.local.database.ExpenseDao
import com.germandebustamante.data.local.mapper.toDomainModel
import com.germandebustamante.data.local.mapper.toDomainModels
import com.germandebustamante.data.local.mapper.toEntity
import com.germandebustamante.data.local.mapper.toEntities
import com.germandebustamante.domain.datasource.LocalExpenseDataSource
import com.germandebustamante.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

@Suppress("TooManyFunctions")
class LocalExpenseDataSourceImpl(
    private val expenseDao: ExpenseDao
) : LocalExpenseDataSource {

    override fun getExpensesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>> {
        return expenseDao.getExpensesByDateRange(startDate.toEpochDays().toLong(), endDate.toEpochDays().toLong())
            .map { it.toDomainModels() }
    }

    override fun getExpensesByDateRangeAndCategory(
        startDate: LocalDate,
        endDate: LocalDate,
        categoryCode: String
    ): Flow<List<Expense>> {
        return expenseDao.getExpensesByDateRangeAndCategory(
            startDate.toEpochDays().toLong(),
            endDate.toEpochDays().toLong(),
            categoryCode
        ).map { it.toDomainModels() }
    }

    override fun getTotalAmountByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<Long?> {
        return expenseDao.getTotalAmountByDateRange(startDate.toEpochDays().toLong(), endDate.toEpochDays().toLong())
    }

    override fun getExpenseById(id: Long): Flow<Expense?> {
        return expenseDao.getExpenseById(id).map { it?.toDomainModel() }
    }

    override fun getAllExpenses(limit: Int): Flow<List<Expense>> {
        return expenseDao.getAllExpenses(limit).map { it.toDomainModels() }
    }

    override suspend fun insertExpense(expense: Expense): Long {
        return expenseDao.insertExpense(expense.toEntity())
    }

    override suspend fun insertExpenses(expenses: List<Expense>): List<Long> {
        return expenseDao.insertExpenses(expenses.toEntities())
    }

    override suspend fun updateExpense(expense: Expense): Int {
        return expenseDao.updateExpense(expense.toEntity())
    }

    override suspend fun deleteExpense(expense: Expense): Int {
        return expenseDao.deleteExpense(expense.toEntity())
    }

    override suspend fun deleteExpenseById(id: Long): Int {
        return expenseDao.deleteExpenseById(id)
    }

    override fun getExpenseCountByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<Int> {
        return expenseDao.getExpenseCountByDateRange(startDate.toEpochDays().toLong(), endDate.toEpochDays().toLong())
    }

    override fun getLastUsedCategoryCode(): Flow<String?> {
        return expenseDao.getLastUsedCategoryCode()
    }
}
