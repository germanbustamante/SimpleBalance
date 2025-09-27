package com.germandebustamante.simplebalance.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.germandebustamante.simplebalance.presentation.ui.home.HomeScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppScreens.Home.route,
        modifier = modifier
    ) {
        composable(AppScreens.Home.route) {
            HomeScreen()
        }
        composable(AppScreens.AddEditExpense.route) {
            // Ticket: Implement AddEditExpenseScreen
        }
    }
}
