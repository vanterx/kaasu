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

data class DailyGroup(
    val dayStartMillis: Long,
    val expenses: List<ExpenseWithCategory>,
    val total: Double
)

class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().let {
        it.get(Calendar.MONTH) to it.get(Calendar.YEAR)
    })

    private val _selectedDay = MutableStateFlow<Long?>(Calendar.getInstance().let {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        c.timeInMillis
    })

    val selectedMonth: StateFlow<Pair<Int, Int>> = _selectedMonth.asStateFlow()
    val selectedDay: StateFlow<Long?> = _selectedDay.asStateFlow()

    val expenses: StateFlow<List<ExpenseWithCategory>> = _selectedMonth
        .combine(expenseRepository.getAllExpenses()) { month, all ->
            val (start, end) = expenseRepository.getMonthRange(month.second, month.first)
            all.filter { it.expense.dateMillis in start until end }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredExpenses: StateFlow<List<ExpenseWithCategory>> = combine(
        expenses, _selectedDay
    ) { all, day ->
        if (day != null) {
            val cal = Calendar.getInstance().apply { timeInMillis = day }
            val start = Calendar.getInstance().apply {
                set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val end = start + 86_400_000L
            all.filter { it.expense.dateMillis in start until end }.sortedByDescending { it.expense.dateMillis }
        } else {
            all
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val dailyGroups: StateFlow<List<DailyGroup>> = expenses.combine(_selectedDay) { all, selectedDay ->
        if (selectedDay != null) {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedDay }
            val start = Calendar.getInstance().apply {
                set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val end = start + 86_400_000L
            val dayExpenses = all.filter { it.expense.dateMillis in start until end }.sortedByDescending { it.expense.dateMillis }
            if (dayExpenses.isEmpty()) emptyList()
            else listOf(DailyGroup(start, dayExpenses, dayExpenses.sumOf { it.expense.amount }))
        } else {
            all.groupBy { expense ->
                val cal = Calendar.getInstance().apply { timeInMillis = expense.expense.dateMillis }
                Calendar.getInstance().apply {
                    set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }.map { (dayStart, list) ->
                DailyGroup(dayStart, list.sortedByDescending { it.expense.dateMillis }, list.sumOf { it.expense.amount })
            }.sortedByDescending { it.dayStartMillis }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val monthlyTotal: StateFlow<Double> = _selectedMonth
        .combine(expenseRepository.getAllExpenses()) { month, all ->
            val (start, end) = expenseRepository.getMonthRange(month.second, month.first)
            all.filter { it.expense.dateMillis in start until end }
                .sumOf { it.expense.amount }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val dayTotal: StateFlow<Double?> = combine(_selectedDay, expenses) { day, all ->
        if (day != null) {
            val cal = Calendar.getInstance().apply { timeInMillis = day }
            val start = Calendar.getInstance().apply {
                set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val end = start + 86_400_000L
            all.filter { it.expense.dateMillis in start until end }.sumOf { it.expense.amount }
        } else null
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _editingExpense = MutableStateFlow<Expense?>(null)

    fun setEditingExpense(expense: Expense?) {
        _editingExpense.value = expense
    }

    fun getEditingExpense(): Expense? = _editingExpense.value

    fun selectMonth(month: Int, year: Int) {
        _selectedMonth.value = month to year
        _selectedDay.value = null
    }

    fun selectDay(dayMillis: Long?) {
        _selectedDay.value = dayMillis
    }

    fun clearDay() {
        _selectedDay.value = null
    }

    fun nextMonth() {
        val (m, y) = _selectedMonth.value
        _selectedDay.value = null
        if (m == 11) _selectedMonth.value = 0 to (y + 1)
        else _selectedMonth.value = (m + 1) to y
    }

    fun previousMonth() {
        val (m, y) = _selectedMonth.value
        _selectedDay.value = null
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
