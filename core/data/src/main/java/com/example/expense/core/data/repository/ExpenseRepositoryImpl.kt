package com.example.expense.core.data.repository

import com.example.expense.core.data.mapper.toDomain
import com.example.expense.core.database.dao.ExpenseDao
import com.example.expense.core.database.entity.ExpenseEntity
import com.example.expense.core.domain.model.AccountTotal
import com.example.expense.core.domain.model.CategoryTotal
import com.example.expense.core.domain.model.ExpenseItem
import com.example.expense.core.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

internal class ExpenseRepositoryImpl(private val dao: ExpenseDao) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<ExpenseItem>> =
        dao.getAllExpenses().map { it.toDomain() }

    override suspend fun getExpenseById(id: Long): ExpenseItem? =
        dao.getExpenseById(id)?.toDomain()

    override fun getMonthlyTotal(monthStart: Long, nextMonthStart: Long): Flow<Double?> =
        dao.getMonthlyTotal(monthStart, nextMonthStart)

    override fun getCategoryTotalsForMonth(monthStart: Long, nextMonthStart: Long): Flow<List<CategoryTotal>> =
        dao.getCategoryTotalsForMonth(monthStart, nextMonthStart)

    override fun getAccountTotalsForRange(start: Long, end: Long): Flow<List<AccountTotal>> =
        dao.getAccountTotalsForRange(start, end)

    override fun getExpensesForMonth(monthStart: Long, nextMonthStart: Long): Flow<List<ExpenseItem>> =
        dao.getExpensesForMonth(monthStart, nextMonthStart).map { it.toDomain() }

    override fun getUncategorizedTotalForMonth(monthStart: Long, nextMonthStart: Long): Flow<Double?> =
        dao.getUncategorizedTotalForMonth(monthStart, nextMonthStart)

    override suspend fun saveExpense(
        amount: Double, description: String, categoryId: Long?, dateMillis: Long, account: String?
    ): Long = dao.insert(
        ExpenseEntity(amount = amount, description = description, categoryId = categoryId,
            dateMillis = dateMillis, account = account)
    )

    override suspend fun updateExpense(
        id: Long, amount: Double, description: String, categoryId: Long?, dateMillis: Long, account: String?
    ) {
        dao.update(
            ExpenseEntity(id = id, amount = amount, description = description,
                categoryId = categoryId, dateMillis = dateMillis, account = account)
        )
    }

    override suspend fun deleteExpense(id: Long) {
        dao.getById(id)?.let { dao.delete(it) }
    }

    override suspend fun getAllExpensesSnapshot(): List<ExpenseItem> =
        dao.getAllExpensesSnapshot().toDomain()

    override fun getMonthRange(year: Int, month: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        return start to calendar.timeInMillis
    }

    override fun getYearToDateRange(year: Int, month: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        calendar.set(year, month + 1, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return start to calendar.timeInMillis
    }

    override fun getCurrentMonthRange(): Pair<Long, Long> {
        val now = Calendar.getInstance()
        return getMonthRange(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
    }
}
