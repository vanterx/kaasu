package com.example.expense.ui.expense

import android.app.DatePickerDialog
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expense.data.model.Category
import com.example.expense.data.model.Expense
import com.example.expense.util.formatDate
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    viewModel: ExpenseViewModel,
    currencyCode: String,
    onNavigateBack: () -> Unit
) {
    val existingExpense = remember { viewModel.getEditingExpense() }
    val categories by viewModel.categories.collectAsState()
    val context = LocalContext.current

    var amount by remember { mutableDoubleStateOf(existingExpense?.amount ?: 0.0) }
    var amountText by remember { mutableStateOf(existingExpense?.let { it.amount.toString() } ?: "") }
    var description by remember { mutableStateOf(existingExpense?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(existingExpense?.categoryId) }
    var dateMillis by remember { mutableLongStateOf(existingExpense?.dateMillis ?: System.currentTimeMillis()) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val editing = viewModel.getEditingExpense()
        amountText = editing?.let { if (it.amount > 0) it.amount.toString() else "" } ?: ""
        amount = editing?.amount ?: 0.0
        description = editing?.description ?: ""
        selectedCategory = editing?.categoryId
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
        }
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

            DatePickerField(
                dateMillis = dateMillis,
                onDateSelected = { dateMillis = it },
                context = context
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

@Composable
private fun DatePickerField(
    dateMillis: Long,
    onDateSelected: (Long) -> Unit,
    context: android.content.Context
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    Calendar.getInstance().apply {
                        set(year, month, dayOfMonth, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                        onDateSelected(timeInMillis)
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CalendarMonth,
                "Select date",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                formatDate(dateMillis),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
