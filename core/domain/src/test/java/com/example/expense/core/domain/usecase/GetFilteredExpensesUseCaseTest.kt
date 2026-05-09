package com.example.expense.core.domain.usecase

import com.example.expense.core.domain.model.AccountTotal
import com.example.expense.core.domain.model.CategoryTotal
import com.example.expense.core.domain.model.ExpenseItem
import com.example.expense.core.domain.repository.ExpenseRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class GetFilteredExpensesUseCaseTest {

    private lateinit var repository: ExpenseRepository
    private lateinit var useCase: GetFilteredExpensesUseCase

    private val zone = ZoneId.systemDefault()

    private fun dayOf(year: Int, month: Int, day: Int): Long =
        LocalDate.of(year, month, day).atStartOfDay(zone).toInstant().toEpochMilli()

    private fun expense(
        id: Long, amount: Double, description: String,
        dateMillis: Long, categoryId: Long? = null, account: String? = null,
        categoryName: String? = null
    ) = ExpenseItem(id, amount, description, dateMillis, account, categoryId, categoryName)

    private val today = dayOf(2026, 5, 10)
    private val yesterday = dayOf(2026, 5, 9)
    private val monthStart = dayOf(2026, 5, 1)
    private val nextMonthStart = dayOf(2026, 6, 1)

    private val allExpenses = listOf(
        expense(1, 50.0, "Groceries", today, categoryId = 1, account = "Cash", categoryName = "Food"),
        expense(2, 20.0, "Bus ticket", yesterday, categoryId = 2, account = "Cash", categoryName = "Transport"),
        expense(3, 100.0, "Dinner out", today, categoryId = 1, account = "Credit", categoryName = "Food"),
        expense(4, 5.0, "Coffee", yesterday, categoryId = null, account = "Cash")
    )

    @Before
    fun setUp() {
        repository = mockk()
        every { repository.getAllExpenses() } returns flowOf(allExpenses)
        useCase = GetFilteredExpensesUseCase(repository)
    }

    private fun execute(
        selectedDay: Long? = null,
        isDateRangeMode: Boolean = false,
        rangeStart: Long? = null,
        rangeEnd: Long? = null,
        searchQuery: String = "",
        categoryFilter: Long? = null,
        accountFilter: String? = null,
        sortMode: GetFilteredExpensesUseCase.SortMode = GetFilteredExpensesUseCase.SortMode.DATE_DESC
    ): Flow<List<ExpenseItem>> = useCase.execute(
        monthStart = monthStart, nextMonthStart = nextMonthStart,
        selectedDay = selectedDay, isDateRangeMode = isDateRangeMode,
        rangeStart = rangeStart, rangeEnd = rangeEnd,
        searchQuery = searchQuery, categoryFilter = categoryFilter,
        accountFilter = accountFilter, sortMode = sortMode
    )

    @Test
    fun `returns all expenses in current month when no filters`() = runTest {
        val result = execute(selectedDay = null).first()
        assertEquals(4, result.size)
    }

    @Test
    fun `filters to selected day only`() = runTest {
        val result = execute(selectedDay = today).first()
        assertEquals(2, result.size)
        assertTrue(result.all { it.dateMillis >= today && it.dateMillis < today + 86_400_000L })
    }

    @Test
    fun `filters by category`() = runTest {
        val result = execute(categoryFilter = 1L).first()
        assertEquals(2, result.size)
        assertTrue(result.all { it.categoryId == 1L })
    }

    @Test
    fun `filters by account`() = runTest {
        val result = execute(accountFilter = "Credit").first()
        assertEquals(1, result.size)
        assertEquals("Dinner out", result[0].description)
    }

    @Test
    fun `filters by search query case-insensitive`() = runTest {
        val result = execute(searchQuery = "coffee").first()
        assertEquals(1, result.size)
        assertEquals("Coffee", result[0].description)
    }

    @Test
    fun `sorts by amount descending`() = runTest {
        val result = execute(sortMode = GetFilteredExpensesUseCase.SortMode.AMOUNT_DESC).first()
        assertEquals(100.0, result[0].amount, 0.0)
        assertEquals(5.0, result.last().amount, 0.0)
    }

    @Test
    fun `sorts by amount ascending`() = runTest {
        val result = execute(sortMode = GetFilteredExpensesUseCase.SortMode.AMOUNT_ASC).first()
        assertEquals(5.0, result[0].amount, 0.0)
        assertEquals(100.0, result.last().amount, 0.0)
    }

    @Test
    fun `date range mode overrides day selection`() = runTest {
        val result = execute(
            isDateRangeMode = true,
            rangeStart = yesterday,
            rangeEnd = yesterday,
            selectedDay = today
        ).first()
        // rangeEnd + 86_400_000 covers the full yesterday
        assertTrue(result.all { it.dateMillis >= yesterday })
        assertEquals(2, result.size)
    }

    @Test
    fun `returns empty when search matches nothing`() = runTest {
        val result = execute(searchQuery = "xyz_no_match").first()
        assertTrue(result.isEmpty())
    }
}
