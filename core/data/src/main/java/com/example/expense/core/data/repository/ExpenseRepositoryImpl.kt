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
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

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
        val ym = YearMonth.of(year, month + 1) // month is 0-based from Calendar convention
        val zone = ZoneId.systemDefault()
        val start = ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = ym.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }

    override fun getYearToDateRange(year: Int, month: Int): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.of(year, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli()
        val ym = YearMonth.of(year, month + 1)
        val end = ym.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }

    override fun getCurrentMonthRange(): Pair<Long, Long> {
        val now = YearMonth.now()
        return getMonthRange(now.year, now.monthValue - 1)
    }

    override fun getCategoryUsageCounts(): Flow<List<com.example.expense.core.domain.model.CategoryUsage>> =
        dao.getCategoryUsageCounts()
}
