package com.germandebustamante.simplebalance.presentation.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LanguagePickerDialog(
    onDismissRequest: () -> Unit,
    onLanguageSelected: (String) -> Unit,
) {
    val languages = mapOf(
        "en" to "English",
        "es" to "Español"
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Select Language") },
        text = {
            Column {
                languages.forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(code) }
                            .padding(12.dp)
                    ) {
                        Text(text = name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = { /* No confirm button needed for selection */ }
    )
}
