package com.example.expense.data.repository

import com.example.expense.data.db.CategoryTotal
import com.example.expense.data.db.ExpenseDao
import com.example.expense.data.model.Expense
import com.example.expense.data.model.ExpenseWithCategory
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ExpenseRepositoryImpl(private val dao: ExpenseDao) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<ExpenseWithCategory>> = dao.getAllExpenses()

    override suspend fun getExpenseById(id: Long): ExpenseWithCategory? = dao.getExpenseById(id)

    override fun getMonthlyTotal(monthStart: Long, nextMonthStart: Long): Flow<Double?> =
        dao.getMonthlyTotal(monthStart, nextMonthStart)

    override fun getCategoryTotalsForMonth(monthStart: Long, nextMonthStart: Long): Flow<List<CategoryTotal>> =
        dao.getCategoryTotalsForMonth(monthStart, nextMonthStart)

    override fun getExpensesForMonth(monthStart: Long, nextMonthStart: Long): Flow<List<ExpenseWithCategory>> =
        dao.getExpensesForMonth(monthStart, nextMonthStart)

    override fun getExpensesByCategory(categoryId: Long): Flow<List<ExpenseWithCategory>> =
        dao.getExpensesByCategory(categoryId)

    override fun getUncategorizedTotalForMonth(monthStart: Long, nextMonthStart: Long): Flow<Double?> =
        dao.getUncategorizedTotalForMonth(monthStart, nextMonthStart)

    override suspend fun saveExpense(expense: Expense): Long = dao.insert(expense)

    override suspend fun updateExpense(expense: Expense) = dao.update(expense)

    override suspend fun deleteExpense(expense: Expense) = dao.delete(expense)

    override suspend fun getAllExpensesSnapshot(): List<ExpenseWithCategory> = dao.getAllExpensesSnapshot()

    override fun getMonthRange(year: Int, month: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        val end = calendar.timeInMillis
        return start to end
    }

    override fun getCurrentMonthRange(): Pair<Long, Long> {
        val now = Calendar.getInstance()
        return getMonthRange(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
    }
}
