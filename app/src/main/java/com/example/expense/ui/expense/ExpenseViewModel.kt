package com.example.expense.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expense.data.model.Category
import com.example.expense.data.model.Expense
import com.example.expense.data.model.ExpenseWithCategory
import com.example.expense.data.repository.CategoryRepository
import com.example.expense.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().let {
        it.get(Calendar.MONTH) to it.get(Calendar.YEAR)
    })

    val selectedMonth: StateFlow<Pair<Int, Int>> = _selectedMonth.asStateFlow()

    val expenses: StateFlow<List<ExpenseWithCategory>> = _selectedMonth
        .combine(expenseRepository.getAllExpenses()) { month, all ->
            val (start, end) = expenseRepository.getMonthRange(month.second, month.first)
            all.filter { it.expense.dateMillis in start until end }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val monthlyTotal: StateFlow<Double> = _selectedMonth
        .combine(expenseRepository.getAllExpenses()) { month, all ->
            val (start, end) = expenseRepository.getMonthRange(month.second, month.first)
            all.filter { it.expense.dateMillis in start until end }
                .sumOf { it.expense.amount }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _editingExpense = MutableStateFlow<Expense?>(null)

    fun setEditingExpense(expense: Expense?) {
        _editingExpense.value = expense
    }

    fun getEditingExpense(): Expense? = _editingExpense.value

    fun selectMonth(month: Int, year: Int) {
        _selectedMonth.value = month to year
    }

    fun nextMonth() {
        val (m, y) = _selectedMonth.value
        if (m == 11) _selectedMonth.value = 0 to (y + 1)
        else _selectedMonth.value = (m + 1) to y
    }

    fun previousMonth() {
        val (m, y) = _selectedMonth.value
        if (m == 0) _selectedMonth.value = 11 to (y - 1)
        else _selectedMonth.value = (m - 1) to y
    }

    fun saveExpense(
        amount: Double,
        description: String,
        categoryId: Long?,
        dateMillis: Long,
        existingExpense: Expense? = null
    ) {
        viewModelScope.launch {
            if (existingExpense != null) {
                expenseRepository.updateExpense(
                    existingExpense.copy(
                        amount = amount,
                        description = description,
                        categoryId = categoryId,
                        dateMillis = dateMillis
                    )
                )
            } else {
                expenseRepository.saveExpense(
                    Expense(
                        amount = amount,
                        description = description,
                        categoryId = categoryId,
                        dateMillis = dateMillis
                    )
                )
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
        }
    }

    class Factory(
        private val expenseRepository: ExpenseRepository,
        private val categoryRepository: CategoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExpenseViewModel(expenseRepository, categoryRepository) as T
        }
    }
}
