package com.germandebustamante.simplebalance.di

import com.germandebustamante.data.local.database.ExpenseDatabase
import com.germandebustamante.data.local.datasource.LocalExpenseDataSourceImpl
import com.germandebustamante.domain.datasource.LocalExpenseDataSource
import com.germandebustamante.data.repository.ExpenseRepositoryImpl
import com.germandebustamante.domain.repository.ExpenseRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single { ExpenseDatabase.create(androidContext()) }

    single { get<ExpenseDatabase>().expenseDao() }

    single<LocalExpenseDataSource> { LocalExpenseDataSourceImpl(get()) }

    single<ExpenseRepository> { ExpenseRepositoryImpl(get()) }
}