package com.germandebustamante.simplebalance.presentation.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.germandebustamante.model.Expense
import com.germandebustamante.simplebalance.R

@Composable
fun ExpenseListItem(
    expense: Expense,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = expense.category.name, style = MaterialTheme.typography.titleMedium)
                expense.note?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
            }
            val amountText = stringResource(R.string.currency_symbol) + expense.amount.toString()
            Text(
                text = amountText,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
