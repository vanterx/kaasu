package com.example.expense.core.domain.usecase

import com.example.expense.core.domain.model.ExpenseItem
import com.example.expense.core.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Filters, sorts, and groups a month's expenses.
 * Extracted from ExpenseViewModel to make this logic unit-testable without Android.
 */
class GetFilteredExpensesUseCase(private val repository: ExpenseRepository) {

    enum class SortMode { DATE_DESC, DATE_ASC, AMOUNT_DESC, AMOUNT_ASC }

    fun execute(
        monthStart: Long,
        nextMonthStart: Long,
        selectedDay: Long?,
        isDateRangeMode: Boolean,
        rangeStart: Long?,
        rangeEnd: Long?,
        searchQuery: String,
        categoryFilter: Long?,
        accountFilter: String?,
        sortMode: SortMode
    ): Flow<List<ExpenseItem>> = repository.getAllExpenses().map { all ->
        var result = when {
            isDateRangeMode && rangeStart != null && rangeEnd != null ->
                all.filter { it.dateMillis in rangeStart until rangeEnd + 86_400_000L }
            else ->
                all.filter { it.dateMillis in monthStart until nextMonthStart }
        }

        if (!isDateRangeMode && selectedDay != null) {
            val start = dayStart(selectedDay)
            result = result.filter { it.dateMillis in start until start + 86_400_000L }
        }

        if (searchQuery.isNotBlank())
            result = result.filter { it.description.contains(searchQuery, ignoreCase = true) }

        if (categoryFilter != null)
            result = result.filter { it.categoryId == categoryFilter }

        if (accountFilter != null)
            result = result.filter { it.account == accountFilter }

        result = when (sortMode) {
            SortMode.DATE_DESC -> result.sortedByDescending { it.dateMillis }
            SortMode.DATE_ASC -> result.sortedBy { it.dateMillis }
            SortMode.AMOUNT_DESC -> result.sortedByDescending { it.amount }
            SortMode.AMOUNT_ASC -> result.sortedBy { it.amount }
        }
        result
    }

    private fun dayStart(millis: Long): Long =
        LocalDate.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
}
