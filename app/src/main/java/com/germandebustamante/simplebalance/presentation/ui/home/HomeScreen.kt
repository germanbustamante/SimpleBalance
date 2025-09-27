package com.germandebustamante.simplebalance.presentation.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.germandebustamante.simplebalance.R
import com.germandebustamante.simplebalance.presentation.viewmodel.HomeViewModel
import com.germandebustamante.simplebalance.util.LocaleManager
import com.germandebustamante.simplebalance.util.LocaleManager.findActivity
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    var showLanguageDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
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
            Text(text = stringResource(R.string.loading_indicator)) // Ticket: Replace with a proper loading indicator
        } else if (uiState.error != null) {
            val errorMessage = stringResource(R.string.error_message_prefix) + uiState.error
            Text(
                text = errorMessage
            ) // Ticket: Replace with a proper error display
        } else if (uiState.hasExpenses) {
            LazyColumn {
                items(uiState.expenses) { expense ->
                    ExpenseListItem(expense = expense)
                }
            }
        } else {
            EmptyState(message = stringResource(R.string.no_expenses_message))
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
