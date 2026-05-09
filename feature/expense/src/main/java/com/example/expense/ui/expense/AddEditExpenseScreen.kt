package com.example.expense.ui.expense

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expense.core.domain.model.Category
import com.example.expense.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    viewModel: ExpenseViewModel,
    currencyCode: String,
    onNavigateBack: () -> Unit
) {
    val existingExpense = remember { viewModel.getEditingExpense() }
    val categories by viewModel.categories.collectAsState()

    var amount by remember { mutableDoubleStateOf(existingExpense?.amount ?: 0.0) }
    var amountText by remember { mutableStateOf(existingExpense?.let { it.amount.toString() } ?: "") }
    var description by remember { mutableStateOf(existingExpense?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(existingExpense?.categoryId) }
    var selectedAccount by remember { mutableStateOf(existingExpense?.account) }
    var dateMillis by remember { mutableLongStateOf(existingExpense?.dateMillis ?: System.currentTimeMillis()) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val editing = viewModel.getEditingExpense()
        amountText = editing?.let { if (it.amount > 0) it.amount.toString() else "" } ?: ""
        amount = editing?.amount ?: 0.0
        description = editing?.description ?: ""
        selectedCategory = editing?.categoryId
        selectedAccount = editing?.account
        dateMillis = editing?.dateMillis ?: System.currentTimeMillis()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingExpense != null) "Edit Expense" else "Add Expense") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = amountText,
                onValueChange = { text ->
                    if (text.all { it.isDigit() || it == '.' }) {
                        amountText = text
                        amount = text.toDoubleOrNull() ?: 0.0
                    }
                },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text(currencyCode) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            CategoryDropdown(
                categories = categories,
                selectedCategoryId = selectedCategory,
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = it },
                onCategorySelected = { category ->
                    selectedCategory = if (category == null) null else category.id
                    categoryDropdownExpanded = false
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            AccountDropdown(
                selectedAccount = selectedAccount,
                expanded = accountDropdownExpanded,
                onExpandedChange = { accountDropdownExpanded = it },
                onAccountSelected = {
                    selectedAccount = it
                    accountDropdownExpanded = false
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            DatePickerField(
                dateMillis = dateMillis,
                onDateSelected = { dateMillis = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (amount > 0) {
                        viewModel.saveExpense(
                            amount = amount,
                            description = description.trim(),
                            categoryId = selectedCategory,
                            dateMillis = dateMillis,
                            account = selectedAccount,
                            existingExpense = existingExpense
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount > 0
            ) {
                Text(if (existingExpense != null) "Update" else "Save")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<Category>,
    selectedCategoryId: Long?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCategorySelected: (Category?) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = categories.find { it.id == selectedCategoryId }?.name ?: "Uncategorized",
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            DropdownMenuItem(
                text = { Text("Uncategorized") },
                onClick = { onCategorySelected(null) }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDropdown(
    selectedAccount: String?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAccountSelected: (String?) -> Unit
) {
    val accounts = listOf("Cash", "Cheque", "Saving", "Credit")
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = selectedAccount ?: "None",
            onValueChange = {},
            readOnly = true,
            label = { Text("Account") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            DropdownMenuItem(
                text = { Text("None") },
                onClick = { onAccountSelected(null) }
            )
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account) },
                    onClick = { onAccountSelected(account) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    dateMillis: Long,
    onDateSelected: (Long) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onDateSelected(it) }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = state) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                onDateSelected(dateMillis - 86_400_000L)
            }) {
                Icon(Icons.Default.ChevronLeft, "Previous day",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showPicker = true }
                    .padding(vertical = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    "Select date",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    formatDate(dateMillis),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            IconButton(onClick = {
                onDateSelected(dateMillis + 86_400_000L)
            }) {
                Icon(Icons.Default.ChevronRight, "Next day",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
