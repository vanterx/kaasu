package com.example.expense.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expense.data.db.CategoryTotal
import com.example.expense.data.repository.ExpenseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class ChartViewModel(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().let {
        it.get(Calendar.MONTH) to it.get(Calendar.YEAR)
    })

    val selectedMonth: StateFlow<Pair<Int, Int>> = _selectedMonth.asStateFlow()

    val categoryTotals: StateFlow<List<CategoryTotal>> = _selectedMonth
        .flatMapLatest { (month, year) ->
            val (start, end) = expenseRepository.getMonthRange(year, month)
            expenseRepository.getCategoryTotalsForMonth(start, end)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val monthlyTotal: StateFlow<Double> = _selectedMonth
        .combine(expenseRepository.getAllExpenses()) { month, all ->
            val (start, end) = expenseRepository.getMonthRange(month.second, month.first)
            all.filter { it.expense.dateMillis in start until end }
                .sumOf { it.expense.amount }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

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

    class Factory(
        private val expenseRepository: ExpenseRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChartViewModel(expenseRepository) as T
        }
    }
}
