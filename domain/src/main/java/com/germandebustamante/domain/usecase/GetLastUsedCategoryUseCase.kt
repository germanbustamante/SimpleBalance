package com.germandebustamante.domain.usecase

import com.germandebustamante.domain.repository.ExpenseRepository
import com.germandebustamante.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case for retrieving the last used expense category.
 *
 * This use case provides the last used category, which can be used for
 * smart defaults in the UI when adding new expenses.
 */
class GetLastUsedCategoryUseCase(
    private val repository: ExpenseRepository
) {

    /**
     * Executes the use case to get the last used category.
     *
     * @return A Flow of the last used Category, or null if no expenses exist.
     */
    fun execute(): Flow<Category?> {
        return repository.getLastUsedCategory()
    }
}
