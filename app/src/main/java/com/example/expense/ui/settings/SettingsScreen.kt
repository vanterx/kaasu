package com.example.expense.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expense.data.model.Category
import com.example.expense.ui.category.CategoryDialog
import com.example.expense.ui.category.CategoryListContent
import com.example.expense.ui.category.CategoryViewModel
import com.example.expense.ui.expense.CurrencyPickerDialog
import com.example.expense.util.PreferencesManager
import kotlinx.coroutines.launch
import java.util.Currency as JavaCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    categoryViewModel: CategoryViewModel,
    preferencesManager: PreferencesManager,
    currencyCode: String,
    onNavigateBack: () -> Unit
) {
    val categories by categoryViewModel.categories.collectAsState()
    val scope = rememberCoroutineScope()
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var deleteConfirmCategory by remember { mutableStateOf<Category?>(null) }

    val currencyName = try {
        JavaCurrency.getInstance(currencyCode).displayName
    } catch (_: Exception) {
        currencyCode
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text("Settings", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, "Add category")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Currency section
            Text(
                "CURRENCY",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        currencyCode,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        currencyName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = { showCurrencyPicker = true }) {
                    Text("Change")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Categories section
            Text(
                "CATEGORIES",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            CategoryListContent(
                categories = categories,
                onEdit = { editingCategory = it },
                onDelete = { deleteConfirmCategory = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            currentCode = currencyCode,
            onDismiss = { showCurrencyPicker = false },
            onSelect = { code ->
                scope.launch {
                    preferencesManager.setCurrencyCode(code)
                }
                showCurrencyPicker = false
            }
        )
    }

    if (showAddDialog || editingCategory != null) {
        CategoryDialog(
            existingCategory = editingCategory,
            onDismiss = {
                showAddDialog = false
                editingCategory = null
            },
            onSave = { name, colorIndex ->
                if (editingCategory != null) {
                    categoryViewModel.updateCategory(editingCategory!!.copy(name = name, colorIndex = colorIndex))
                } else {
                    categoryViewModel.addCategory(name, colorIndex)
                }
                showAddDialog = false
                editingCategory = null
            }
        )
    }

    deleteConfirmCategory?.let { category ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { deleteConfirmCategory = null },
            title = { Text("Delete Category") },
            text = { Text("Delete \"${category.name}\"?\nExpenses in this category will become uncategorized.") },
            confirmButton = {
                TextButton(onClick = {
                    categoryViewModel.deleteCategory(category)
                    deleteConfirmCategory = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmCategory = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
