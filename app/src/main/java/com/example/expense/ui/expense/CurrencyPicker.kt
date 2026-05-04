package com.example.expense.ui.expense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val supportedCurrencies = listOf(
    "NZD" to "NZD — New Zealand Dollar",
    "USD" to "USD — US Dollar",
    "EUR" to "EUR — Euro",
    "AUD" to "AUD — Australian Dollar",
    "GBP" to "GBP — British Pound",
    "JPY" to "JPY — Japanese Yen",
    "CAD" to "CAD — Canadian Dollar",
    "INR" to "INR — Indian Rupee",
    "SGD" to "SGD — Singapore Dollar",
    "CNY" to "CNY — Chinese Yuan"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPickerDialog(
    currentCode: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(currentCode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency") },
        text = {
            Column {
                Text(
                    "Choose your preferred currency for displaying amounts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = supportedCurrencies.find { it.first == selected }?.second
                            ?: supportedCurrencies.first().second,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Currency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        supportedCurrencies.forEach { (code, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selected = code
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelect(selected) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
