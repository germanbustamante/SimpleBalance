package com.germandebustamante.simplebalance.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.germandebustamante.simplebalance.presentation.ui.addeditexpense.AddEditExpenseScreen
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
            HomeScreen(navController = navController)
        }
        composable(
            route = AppScreens.AddEditExpense.route,
            arguments = listOf(
                navArgument("expenseId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getLong("expenseId")
            AddEditExpenseScreen(navController = navController, expenseId = expenseId)
        }
    }
}
