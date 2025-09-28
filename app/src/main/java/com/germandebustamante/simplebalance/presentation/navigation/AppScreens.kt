package com.germandebustamante.simplebalance.presentation.navigation

sealed class AppScreens(val route: String) {
    object Home : AppScreens("home_screen")
    object AddEditExpense : AppScreens("add_edit_expense_screen?expenseId={expenseId}") {
        fun createRoute(expenseId: Long = 0L) = "add_edit_expense_screen?expenseId=$expenseId"
    }
}
