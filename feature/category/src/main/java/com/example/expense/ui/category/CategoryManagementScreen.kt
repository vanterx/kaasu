package com.example.expense.ui.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.example.expense.core.domain.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    viewModel: CategoryViewModel,
    onNavigateBack: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val usageCounts by viewModel.categoryUsageCounts.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var deleteConfirmCategory by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text("Categories", style = MaterialTheme.typography.titleMedium) },
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
        CategoryListContent(
            categories = categories,
            usageCounts = usageCounts,
            onEdit = { editingCategory = it },
            onDelete = { deleteConfirmCategory = it },
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
        )
    }

    if (showAddDialog) {
        CategoryDialog(
            existingCategory = null,
            existingCategories = categories,
            onDismiss = { showAddDialog = false },
            onSave = { name, colorIndex ->
                viewModel.addCategory(name, colorIndex)
                showAddDialog = false
            }
        )
    }

    editingCategory?.let { cat ->
        CategoryDialog(
            existingCategory = cat,
            existingCategories = categories,
            onDismiss = { editingCategory = null },
            onSave = { name, colorIndex ->
                viewModel.updateCategory(cat.copy(name = name, colorIndex = colorIndex))
                editingCategory = null
            }
        )
    }

    deleteConfirmCategory?.let { cat ->
        AlertDialog(
            onDismissRequest = { deleteConfirmCategory = null },
            title = { Text("Delete Category") },
            text = {
                Text("Delete \"${cat.name}\"? Expenses in this category will become uncategorized.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(cat)
                    deleteConfirmCategory = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmCategory = null }) { Text("Cancel") }
            }
        )
    }
}
