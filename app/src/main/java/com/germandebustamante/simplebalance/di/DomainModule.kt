package com.germandebustamante.simplebalance.di

import com.germandebustamante.domain.usecase.AddExpenseUseCase
import com.germandebustamante.domain.usecase.GetMonthlyExpensesUseCase
import com.germandebustamante.domain.usecase.GetMonthlyTotalUseCase
import com.germandebustamante.domain.usecase.GetLastUsedCategoryUseCase
import com.germandebustamante.domain.usecase.UpdateExpenseUseCase
import com.germandebustamante.domain.usecase.ValidateExpenseUseCase
import com.germandebustamante.domain.usecase.GetExpenseByIdUseCase
import com.germandebustamante.domain.usecase.DeleteExpenseUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { AddExpenseUseCase(get()) }
    factory { GetMonthlyExpensesUseCase(get()) }
    factory { GetMonthlyTotalUseCase(get()) }
    factory { UpdateExpenseUseCase(get()) }
    factory { GetLastUsedCategoryUseCase(get()) }
    factory { ValidateExpenseUseCase() }
    factory { GetExpenseByIdUseCase(get()) }
    factory { DeleteExpenseUseCase(get()) }
}
