package com.germandebustamante.simplebalance.di

import com.germandebustamante.domain.usecase.GetExpenseByIdUseCase
import com.germandebustamante.simplebalance.presentation.viewmodel.AddEditExpenseViewModel
import com.germandebustamante.simplebalance.presentation.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { AddEditExpenseViewModel(get(), get(), get(), get(), get(), get<GetExpenseByIdUseCase>()) }
}
