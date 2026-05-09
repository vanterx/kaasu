package com.example.expense.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expense.core.domain.model.Category
import com.example.expense.core.domain.model.ExpenseItem
import com.example.expense.core.domain.repository.CategoryRepository
import com.example.expense.core.domain.repository.ExpenseRepository
import com.example.expense.core.domain.usecase.DeleteExpenseUseCase
import com.example.expense.core.domain.usecase.GetFilteredExpensesUseCase
import com.example.expense.core.domain.usecase.SaveExpenseUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.example.expense.util.dayStartMillis
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId

enum class SortMode(val label: String) {
    DATE_DESC("Newest first"),
    DATE_ASC("Oldest first"),
    AMOUNT_DESC("Highest amount"),
    AMOUNT_ASC("Lowest amount")
}

data class DailyGroup(
    val dayStartMillis: Long,
    val expenses: List<ExpenseItem>,
    val total: Double
)

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val getFilteredExpensesUseCase: GetFilteredExpensesUseCase,
    private val saveExpenseUseCase: SaveExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now().let {
        it.monthValue - 1 to it.year // month kept 0-based for repo compatibility
    })

    private val _selectedDay = MutableStateFlow<Long?>(dayStartMillis(System.currentTimeMillis()))

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategoryFilter = MutableStateFlow<Long?>(null)
    private val _selectedAccountFilter = MutableStateFlow<String?>(null)
    private val _sortMode = MutableStateFlow(SortMode.DATE_DESC)
    private val _isDateRangeMode = MutableStateFlow(false)
    private val _dateRangeStart = MutableStateFlow<Long?>(null)
    private val _dateRangeEnd = MutableStateFlow<Long?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val selectedMonth: StateFlow<Pair<Int, Int>> = _selectedMonth.asStateFlow()
    val selectedDay: StateFlow<Long?> = _selectedDay.asStateFlow()
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val selectedCategoryFilter: StateFlow<Long?> = _selectedCategoryFilter.asStateFlow()
    val selectedAccountFilter: StateFlow<String?> = _selectedAccountFilter.asStateFlow()
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()
    val isDateRangeMode: StateFlow<Boolean> = _isDateRangeMode.asStateFlow()
    val dateRangeStart: StateFlow<Long?> = _dateRangeStart.asStateFlow()
    val dateRangeEnd: StateFlow<Long?> = _dateRangeEnd.asStateFlow()
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() { _errorMessage.value = null }

    private data class FilterParams(
        val month: Int, val year: Int,
        val isRange: Boolean, val rangeStart: Long?, val rangeEnd: Long?,
        val selectedDay: Long?, val query: String,
        val catFilter: Long?, val acctFilter: String?,
        val sort: SortMode
    )

    private val _filterParams = combine(
        _selectedMonth, _isDateRangeMode, _dateRangeStart, _dateRangeEnd,
        _selectedDay, _searchQuery, _selectedCategoryFilter, _selectedAccountFilter, _sortMode
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val month = values[0] as Pair<Int, Int>
        val isRange = values[1] as Boolean
        val rangeStart = values[2] as Long?
        val rangeEnd = values[3] as Long?
        val selectedDay = values[4] as Long?
        val query = values[5] as String
        val catFilter = values[6] as Long?
        val acctFilter = values[7] as String?
        val sort = values[8] as SortMode
        FilterParams(month.first, month.second, isRange, rangeStart, rangeEnd,
            selectedDay, query, catFilter, acctFilter, sort)
    }

    val filteredExpenses: StateFlow<List<ExpenseItem>> = _filterParams.flatMapLatest { p ->
        val (start, end) = expenseRepository.getMonthRange(p.year, p.month)
        getFilteredExpensesUseCase.execute(
            monthStart = start, nextMonthStart = end,
            selectedDay = p.selectedDay,
            isDateRangeMode = p.isRange,
            rangeStart = p.rangeStart, rangeEnd = p.rangeEnd,
            searchQuery = p.query,
            categoryFilter = p.catFilter,
            accountFilter = p.acctFilter,
            sortMode = when (p.sort) {
                SortMode.DATE_DESC -> GetFilteredExpensesUseCase.SortMode.DATE_DESC
                SortMode.DATE_ASC -> GetFilteredExpensesUseCase.SortMode.DATE_ASC
                SortMode.AMOUNT_DESC -> GetFilteredExpensesUseCase.SortMode.AMOUNT_DESC
                SortMode.AMOUNT_ASC -> GetFilteredExpensesUseCase.SortMode.AMOUNT_ASC
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val dailyGroups: StateFlow<List<DailyGroup>> = filteredExpenses.map { list ->
        list.groupBy { expense ->
            dayStartMillis(expense.dateMillis)
        }.map { (dayStart, items) ->
            DailyGroup(dayStart, items, items.sumOf { it.amount })
        }.sortedByDescending { it.dayStartMillis }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val monthlyTotal: StateFlow<Double> = filteredExpenses.map { list ->
        list.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val dayTotal: StateFlow<Double?> = combine(_selectedDay, filteredExpenses, _isDateRangeMode) { day, all, isRange ->
        if (isRange || day == null) null
        else {
            val start = dayStartMillis(day)
            all.filter { it.dateMillis in start until start + 86_400_000L }.sumOf { it.amount }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val rangeTotal: StateFlow<Double?> = combine(filteredExpenses, _isDateRangeMode) { list, isRange ->
        if (isRange) list.sumOf { it.amount } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _editingExpense = MutableStateFlow<ExpenseItem?>(null)

    fun setEditingExpense(expense: ExpenseItem?) { _editingExpense.value = expense }
    fun getEditingExpense(): ExpenseItem? = _editingExpense.value

    fun selectMonth(month: Int, year: Int) { _selectedMonth.value = month to year; _selectedDay.value = null }
    fun selectDay(dayMillis: Long?) { _selectedDay.value = dayMillis }
    fun clearDay() { _selectedDay.value = null }

    fun nextMonth() {
        val (m, y) = _selectedMonth.value
        _selectedDay.value = null
        _selectedMonth.value = if (m == 11) 0 to (y + 1) else (m + 1) to y
    }

    fun previousMonth() {
        val (m, y) = _selectedMonth.value
        _selectedDay.value = null
        _selectedMonth.value = if (m == 0) 11 to (y - 1) else (m - 1) to y
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setCategoryFilter(categoryId: Long?) { _selectedCategoryFilter.value = categoryId }
    fun setAccountFilter(account: String?) { _selectedAccountFilter.value = account }

    fun cycleSortMode() {
        val modes = SortMode.entries.toTypedArray()
        _sortMode.value = modes[(modes.indexOf(_sortMode.value) + 1) % modes.size]
    }

    fun toggleDateRangeMode() {
        val newMode = !_isDateRangeMode.value
        _isDateRangeMode.value = newMode
        if (newMode && (_dateRangeStart.value == null || _dateRangeEnd.value == null)) {
            val zone = ZoneId.systemDefault()
            val ym = YearMonth.now()
            _dateRangeStart.value = ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            _dateRangeEnd.value = ym.atEndOfMonth().atStartOfDay(zone).toInstant().toEpochMilli()
        }
    }

    fun setDateRangeStart(start: Long?) {
        _dateRangeStart.value = start
        val end = _dateRangeEnd.value
        if (start != null && end != null && start > end) _dateRangeEnd.value = start
    }

    fun setDateRangeEnd(end: Long?) {
        _dateRangeEnd.value = end
        val start = _dateRangeStart.value
        if (start != null && end != null && end < start) _dateRangeStart.value = end
    }

    fun saveExpense(
        amount: Double,
        description: String,
        categoryId: Long?,
        dateMillis: Long,
        account: String? = null,
        existingExpense: ExpenseItem? = null
    ) {
        viewModelScope.launch {
            if (existingExpense != null) {
                saveExpenseUseCase.update(existingExpense.id, amount, description, categoryId, dateMillis, account)
                    .onFailure { _errorMessage.value = "Failed to save expense" }
            } else {
                saveExpenseUseCase.save(amount, description, categoryId, dateMillis, account)
                    .onFailure { _errorMessage.value = "Failed to save expense" }
            }
        }
    }

    fun deleteExpense(expense: ExpenseItem) {
        viewModelScope.launch {
            deleteExpenseUseCase(expense.id)
                .onFailure { _errorMessage.value = "Failed to delete expense" }
        }
    }

    companion object {
        val accountTypes = listOf("Cash", "Cheque", "Saving", "Credit")
    }

    class Factory(
        private val expenseRepository: ExpenseRepository,
        private val categoryRepository: CategoryRepository,
        private val getFilteredExpensesUseCase: GetFilteredExpensesUseCase,
        private val saveExpenseUseCase: SaveExpenseUseCase,
        private val deleteExpenseUseCase: DeleteExpenseUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ExpenseViewModel(expenseRepository, categoryRepository,
                getFilteredExpensesUseCase, saveExpenseUseCase, deleteExpenseUseCase) as T
    }
}
