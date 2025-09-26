package com.germandebustamante.simplebalance.di

import android.content.Context
import com.germandebustamante.simplebalance.data.local.database.ExpenseDatabase
import com.germandebustamante.simplebalance.data.repository.ExpenseRepositoryImpl
import com.germandebustamante.simplebalance.domain.repository.ExpenseRepository
import com.germandebustamante.simplebalance.domain.usecase.AddExpenseUseCase
import com.germandebustamante.simplebalance.domain.usecase.GetMonthlyExpensesUseCase
import com.germandebustamante.simplebalance.domain.usecase.GetMonthlyTotalUseCase

/**
 * Manual dependency injection container for Phase 1.
 * 
 * This container provides all dependencies for the application following
 * clean architecture principles. It's designed to be easily replaceable
 * with Hilt in future phases without breaking existing code.
 * 
 * Design principles:
 * - Lazy initialization for better app startup performance
 * - Single instance pattern for database and repository
 * - Interface-based dependencies for testability
 * - Future-proof structure for Hilt migration
 */
open class AppContainer(
    private val applicationContext: Context
) {
    
    // ===============================
    // Data Layer Dependencies
    // ===============================
    
    /**
     * Room database instance - singleton with lazy initialization.
     * Uses KSP for code generation instead of KAPT for better performance.
     */
    open val database: ExpenseDatabase by lazy {
        ExpenseDatabase.create(applicationContext)
    }
    
    /**
     * Expense repository implementation - singleton.
     * Implements domain repository interface for clean architecture.
     */
    val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepositoryImpl(database.expenseDao())
    }
    
    // ===============================
    // Domain Layer Dependencies
    // ===============================
    
    /**
     * Use case for adding new expenses.
     * Contains business logic and validation rules.
     */
    val addExpenseUseCase: AddExpenseUseCase by lazy {
        AddExpenseUseCase(expenseRepository)
    }
    
    /**
     * Use case for retrieving monthly expenses.
     * Handles sorting, filtering, and business transformations.
     */
    val getMonthlyExpensesUseCase: GetMonthlyExpensesUseCase by lazy {
        GetMonthlyExpensesUseCase(expenseRepository)
    }
    
    /**
     * Use case for calculating monthly totals.
     * Provides totals, breakdowns, and comparisons.
     */
    val getMonthlyTotalUseCase: GetMonthlyTotalUseCase by lazy {
        GetMonthlyTotalUseCase(expenseRepository)
    }
    
    // ===============================
    // Future Use Cases (Commented for Phase 1)
    // ===============================
    
    // Phase 2+: Additional use cases will be added here
    // val updateExpenseUseCase: UpdateExpenseUseCase by lazy { ... }
    // val deleteExpenseUseCase: DeleteExpenseUseCase by lazy { ... }
    // val getLastUsedCategoryUseCase: GetLastUsedCategoryUseCase by lazy { ... }
    
    // ===============================
    // Testing Support
    // ===============================
    
    companion object {
        /**
         * Create test container with in-memory database.
         * Used for integration testing without persistent storage.
         * 
         * @param context Test context
         * @return AppContainer configured for testing
         */
        fun createTestContainer(context: Context): AppContainer {
            return TestAppContainer(context)
        }
    }
}

/**
 * Test-specific app container with in-memory database.
 * 
 * This container is used for testing to avoid persistent storage
 * and ensure test isolation. It maintains the same interface as
 * the production container for consistency.
 */
private class TestAppContainer(
    private val testContext: Context
) : AppContainer(testContext) {
    
    /**
     * In-memory database for testing.
     * Data doesn't persist between test runs, ensuring isolation.
     */
    override val database: ExpenseDatabase by lazy {
        ExpenseDatabase.createInMemory(testContext)
    }
}

/**
 * Extension property to easily access the app container from Application.
 * This makes it easy to retrieve dependencies throughout the app.
 * 
 * Usage:
 * ```kotlin
 * class MyApplication : Application() {
 *     val container by lazy { AppContainer(this) }
 * }
 * 
 * // In Activity/Fragment/ViewModel:
 * val useCase = (application as MyApplication).container.addExpenseUseCase
 * ```
 */
val Context.appContainer: AppContainer
    get() = (applicationContext as SimpleBalanceApplication).container

/**
 * Application class placeholder.
 * This will be implemented in Phase 2 when we create the presentation layer.
 * 
 * For now, this serves as documentation of the intended structure.
 */
// TODO: Phase 2 - Create actual Application class
abstract class SimpleBalanceApplication : android.app.Application() {
    abstract val container: AppContainer
}