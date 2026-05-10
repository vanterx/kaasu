package com.example.expense.core.domain.repository

import com.example.expense.core.domain.model.AccountTotal
import com.example.expense.core.domain.model.CategoryTotal
import com.example.expense.core.domain.model.CategoryUsage
import com.example.expense.core.domain.model.ExpenseItem
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<ExpenseItem>>
    suspend fun getExpenseById(id: Long): ExpenseItem?
    fun getMonthlyTotal(monthStart: Long, nextMonthStart: Long): Flow<Double?>
    fun getCategoryTotalsForMonth(monthStart: Long, nextMonthStart: Long): Flow<List<CategoryTotal>>
    fun getAccountTotalsForRange(start: Long, end: Long): Flow<List<AccountTotal>>
    fun getExpensesForMonth(monthStart: Long, nextMonthStart: Long): Flow<List<ExpenseItem>>
    fun getUncategorizedTotalForMonth(monthStart: Long, nextMonthStart: Long): Flow<Double?>
    suspend fun saveExpense(
        amount: Double,
        description: String,
        categoryId: Long?,
        dateMillis: Long,
        account: String?
    ): Long
    suspend fun updateExpense(
        id: Long,
        amount: Double,
        description: String,
        categoryId: Long?,
        dateMillis: Long,
        account: String?
    )
    suspend fun deleteExpense(id: Long)
    suspend fun getAllExpensesSnapshot(): List<ExpenseItem>
    fun getMonthRange(year: Int, month: Int): Pair<Long, Long>
    fun getYearToDateRange(year: Int, month: Int): Pair<Long, Long>
    fun getCurrentMonthRange(): Pair<Long, Long>
    fun getCategoryUsageCounts(): Flow<List<CategoryUsage>>
}
