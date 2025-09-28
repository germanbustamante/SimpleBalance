package com.germandebustamante.simplebalance.presentation.ui.home

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.germandebustamante.simplebalance.R
import com.germandebustamante.simplebalance.presentation.navigation.AppScreens
import com.germandebustamante.simplebalance.presentation.viewmodel.HomeEvent
import com.germandebustamante.simplebalance.presentation.viewmodel.HomeViewModel
import com.germandebustamante.simplebalance.util.LocaleManager
import com.germandebustamante.simplebalance.util.LocaleManager.findActivity
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    var showLanguageDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(AppScreens.AddEditExpense.createRoute()) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_expense_content_description))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonthSelector(
                    currentMonth = currentMonth,
                    onPreviousMonthClick = viewModel::goToPreviousMonth,
                    onNextMonthClick = viewModel::goToNextMonth,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showLanguageDialog = true }) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = stringResource(R.string.select_language_content_description)
                    )
                }
            }

            MonthlyTotalDisplay(totalAmount = uiState.monthlyTotal)

            if (uiState.isLoading) {
                Text(
                    text = stringResource(R.string.loading_indicator)
                ) // Ticket: Replace with a proper loading indicator
            } else if (uiState.error != null) {
                val errorMessage = stringResource(R.string.error_message_prefix) + uiState.error
                Text(
                    text = errorMessage
                ) // Ticket: Replace with a proper error display
            } else if (uiState.hasExpenses) {
                val filteredExpenses = uiState.expenses.filter { it.id != uiState.pendingDeletedExpenseId }
                val groupedExpenses = filteredExpenses.groupBy { it.date }
                LazyColumn {
                    groupedExpenses.forEach { (date, expenses) ->
                        stickyHeader { DateHeader(date = date) }
                        items(expenses, key = { it.id }) { expense ->
                            SwipeToDeleteContainer(
                                item = expense,
                                onDelete = {
                                    Log.i("HomeScreen", "Deleting expense: $expense")
                                    viewModel.onEvent(HomeEvent.DeleteExpense(expense))
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = context.getString(R.string.expense_deleted_message),
                                            actionLabel = context.getString(R.string.undo_action),
                                            withDismissAction = true,
                                            duration = SnackbarDuration.Short
                                        )
                                        when (result) {
                                            SnackbarResult.ActionPerformed -> {
                                                viewModel.onEvent(HomeEvent.UndoDelete)
                                            }

                                            SnackbarResult.Dismissed ->
                                                viewModel.onEvent(HomeEvent.OnDeleteSnackBarDismiss)
                                        }
                                    }
                                }
                            ) {
                                ExpenseListItem(
                                    expense = expense,
                                    onClick = {
                                        navController.navigate(
                                            AppScreens.AddEditExpense.createRoute(expense.id)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                EmptyState(message = stringResource(R.string.no_expenses_message))
            }
        }
    }

    if (showLanguageDialog) {
        LanguagePickerDialog(
            onDismissRequest = { showLanguageDialog = false },
            onLanguageSelected = { languageCode ->
                LocaleManager.setLocale(context.findActivity() ?: context, languageCode)
                showLanguageDialog = false
            }
        )
    }
}
