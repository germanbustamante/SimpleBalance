package com.germandebustamante.simplebalance.presentation.navigation

sealed class AppScreens(val route: String) {
    object Home : AppScreens("home_screen")
    object AddEditExpense : AppScreens("add_edit_expense_screen")
}
