package com.example.expense.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expense.core.domain.model.AccountTotal
import com.example.expense.core.domain.model.CategoryTotal
import com.example.expense.core.domain.repository.ExpenseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class ChartViewModel(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now().let {
        it.monthValue - 1 to it.year // month kept 0-based for repo compatibility
    })

    private val _isYtdMode = MutableStateFlow(false)

    val selectedMonth: StateFlow<Pair<Int, Int>> = _selectedMonth.asStateFlow()
    val isYtdMode: StateFlow<Boolean> = _isYtdMode.asStateFlow()

    val categoryTotals: StateFlow<List<CategoryTotal>> =
        combine(_selectedMonth, _isYtdMode) { month, ytd ->
            month to ytd
        }.flatMapLatest { pair ->
            val (month, year) = pair.first
            val isYtd = pair.second
            val (start, end) = if (isYtd) {
                expenseRepository.getYearToDateRange(year, month)
            } else {
                expenseRepository.getMonthRange(year, month)
            }
            expenseRepository.getCategoryTotalsForMonth(start, end)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val accountTotals: StateFlow<List<AccountTotal>> =
        combine(_selectedMonth, _isYtdMode) { month, ytd ->
            month to ytd
        }.flatMapLatest { pair ->
            val (month, year) = pair.first
            val isYtd = pair.second
            val (start, end) = if (isYtd) {
                expenseRepository.getYearToDateRange(year, month)
            } else {
                expenseRepository.getMonthRange(year, month)
            }
            expenseRepository.getAccountTotalsForRange(start, end)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val monthlyTotal: StateFlow<Double> = combine(
        _selectedMonth, _isYtdMode
    ) { month, ytd ->
        month to ytd
    }.combine(expenseRepository.getAllExpenses()) { pair, all ->
        val (month, year) = pair.first
        val isYtd = pair.second
        val (start, end) = if (isYtd) {
            expenseRepository.getYearToDateRange(year, month)
        } else {
            expenseRepository.getMonthRange(year, month)
        }
        all.filter { it.dateMillis in start until end }
            .sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

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

    fun toggleYtdMode() {
        _isYtdMode.value = !_isYtdMode.value
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
