package com.germandebustamante.domain.usecase

import com.germandebustamante.model.Expense
import com.germandebustamante.domain.repository.ExpenseRepository

class DeleteExpenseUseCase(private val expenseRepository: ExpenseRepository) {
    suspend operator fun invoke(expense: Expense) = expenseRepository.deleteExpense(expense.id)
}
