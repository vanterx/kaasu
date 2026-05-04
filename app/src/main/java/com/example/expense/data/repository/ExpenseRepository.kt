package com.example.expense.data.repository

import com.example.expense.data.db.CategoryTotal
import com.example.expense.data.db.ExpenseDao
import com.example.expense.data.model.Expense
import com.example.expense.data.model.ExpenseWithCategory
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ExpenseRepository(private val dao: ExpenseDao) {

    fun getAllExpenses(): Flow<List<ExpenseWithCategory>> = dao.getAllExpenses()

    suspend fun getExpenseById(id: Long): ExpenseWithCategory? = dao.getExpenseById(id)

    fun getMonthlyTotal(monthStart: Long, nextMonthStart: Long): Flow<Double?> =
        dao.getMonthlyTotal(monthStart, nextMonthStart)

    fun getCategoryTotalsForMonth(monthStart: Long, nextMonthStart: Long): Flow<List<CategoryTotal>> =
        dao.getCategoryTotalsForMonth(monthStart, nextMonthStart)

    fun getExpensesForMonth(monthStart: Long, nextMonthStart: Long): Flow<List<ExpenseWithCategory>> =
        dao.getExpensesForMonth(monthStart, nextMonthStart)

    fun getExpensesByCategory(categoryId: Long): Flow<List<ExpenseWithCategory>> =
        dao.getExpensesByCategory(categoryId)

    fun getUncategorizedTotalForMonth(monthStart: Long, nextMonthStart: Long): Flow<Double?> =
        dao.getUncategorizedTotalForMonth(monthStart, nextMonthStart)

    suspend fun saveExpense(expense: Expense): Long = dao.insert(expense)

    suspend fun updateExpense(expense: Expense) = dao.update(expense)

    suspend fun deleteExpense(expense: Expense) = dao.delete(expense)

    suspend fun getAllExpensesSnapshot(): List<ExpenseWithCategory> = dao.getAllExpensesSnapshot()

    fun getMonthRange(year: Int, month: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        val end = calendar.timeInMillis
        return start to end
    }

    fun getCurrentMonthRange(): Pair<Long, Long> {
        val now = Calendar.getInstance()
        return getMonthRange(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
    }
}
