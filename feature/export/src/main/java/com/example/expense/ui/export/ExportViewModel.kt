package com.example.expense.ui.export

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expense.core.domain.repository.ExpenseRepository
import com.example.expense.util.CsvExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExportViewModel(private val expenseRepository: ExpenseRepository) : ViewModel() {

    private val _exportResult = MutableStateFlow<String?>(null)
    val exportResult: StateFlow<String?> = _exportResult.asStateFlow()

    fun export(context: Context, uri: Uri) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val expenses = expenseRepository.getAllExpensesSnapshot()
                    CsvExporter(context).export(expenses, uri)
                    expenses.size
                }
            }.onSuccess { count ->
                _exportResult.value = "Exported $count expenses successfully!"
            }.onFailure { e ->
                _exportResult.value = "Export failed: ${e.message}"
            }
        }
    }

    fun clearResult() { _exportResult.value = null }

    class Factory(private val expenseRepository: ExpenseRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ExportViewModel(expenseRepository) as T
    }
}
