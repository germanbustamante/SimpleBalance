package com.germandebustamante.domain.usecase

import com.germandebustamante.domain.repository.ExpenseRepository
import com.germandebustamante.model.Expense
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving a single expense by its ID.
 */
class GetExpenseByIdUseCase(
    private val repository: ExpenseRepository
) {

    /**
     * Executes the use case to get an expense by its ID.
     *
     * @param expenseId The ID of the expense to retrieve.
     * @return A Flow of the Expense, or null if not found.
     */
    fun execute(expenseId: Long): Flow<Expense?> {
        return repository.getExpenseById(expenseId)
    }
}
