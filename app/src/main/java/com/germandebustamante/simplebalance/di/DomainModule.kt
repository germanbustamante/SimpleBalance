package com.germandebustamante.simplebalance.di

import com.germandebustamante.domain.usecase.AddExpenseUseCase
import com.germandebustamante.domain.usecase.GetMonthlyExpensesUseCase
import com.germandebustamante.domain.usecase.GetMonthlyTotalUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { AddExpenseUseCase(get()) }
    factory { GetMonthlyExpensesUseCase(get()) }
    factory { GetMonthlyTotalUseCase(get()) }
}
