package com.example.expense.data.repository

import com.example.expense.data.db.CategoryTotal
import com.example.expense.data.model.Expense
import com.example.expense.data.model.ExpenseWithCategory
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<ExpenseWithCategory>>
    suspend fun getExpenseById(id: Long): ExpenseWithCategory?
    fun getMonthlyTotal(monthStart: Long, nextMonthStart: Long): Flow<Double?>
    fun getCategoryTotalsForMonth(monthStart: Long, nextMonthStart: Long): Flow<List<CategoryTotal>>
    fun getExpensesForMonth(monthStart: Long, nextMonthStart: Long): Flow<List<ExpenseWithCategory>>
    fun getExpensesByCategory(categoryId: Long): Flow<List<ExpenseWithCategory>>
    fun getUncategorizedTotalForMonth(monthStart: Long, nextMonthStart: Long): Flow<Double?>
    suspend fun saveExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    suspend fun getAllExpensesSnapshot(): List<ExpenseWithCategory>
    fun getMonthRange(year: Int, month: Int): Pair<Long, Long>
    fun getCurrentMonthRange(): Pair<Long, Long>
}
