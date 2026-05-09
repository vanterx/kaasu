package com.example.expense.core.domain.usecase

import com.example.expense.core.domain.repository.ExpenseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveExpenseUseCaseTest {

    private lateinit var repository: ExpenseRepository
    private lateinit var useCase: SaveExpenseUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = SaveExpenseUseCase(repository)
    }

    @Test
    fun `save succeeds with valid amount`() = runTest {
        coEvery { repository.saveExpense(any(), any(), any(), any(), any()) } returns 1L
        val result = useCase.save(10.0, "Coffee", null, 0L, null)
        assertTrue(result.isSuccess)
        coVerify { repository.saveExpense(10.0, "Coffee", null, 0L, null) }
    }

    @Test
    fun `save trims description whitespace`() = runTest {
        coEvery { repository.saveExpense(any(), any(), any(), any(), any()) } returns 1L
        useCase.save(10.0, "  Coffee  ", null, 0L, null)
        coVerify { repository.saveExpense(10.0, "Coffee", null, 0L, null) }
    }

    @Test
    fun `save fails when amount is zero`() = runTest {
        val result = useCase.save(0.0, "Coffee", null, 0L, null)
        assertTrue(result.isFailure)
    }

    @Test
    fun `save fails when amount is negative`() = runTest {
        val result = useCase.save(-5.0, "Coffee", null, 0L, null)
        assertTrue(result.isFailure)
    }

    @Test
    fun `update succeeds with valid amount`() = runTest {
        coEvery { repository.updateExpense(any(), any(), any(), any(), any(), any()) } returns Unit
        val result = useCase.update(1L, 20.0, "Lunch", 1L, 0L, "Cash")
        assertTrue(result.isSuccess)
        coVerify { repository.updateExpense(1L, 20.0, "Lunch", 1L, 0L, "Cash") }
    }

    @Test
    fun `update fails when amount is zero`() = runTest {
        val result = useCase.update(1L, 0.0, "Lunch", null, 0L, null)
        assertTrue(result.isFailure)
    }
}
