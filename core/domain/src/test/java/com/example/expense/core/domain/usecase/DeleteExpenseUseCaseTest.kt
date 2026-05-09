package com.example.expense.core.domain.usecase

import com.example.expense.core.domain.repository.ExpenseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteExpenseUseCaseTest {

    private lateinit var repository: ExpenseRepository
    private lateinit var useCase: DeleteExpenseUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = DeleteExpenseUseCase(repository)
    }

    @Test
    fun `delete succeeds and delegates to repository`() = runTest {
        coEvery { repository.deleteExpense(42L) } returns Unit
        val result = useCase(42L)
        assertTrue(result.isSuccess)
        coVerify { repository.deleteExpense(42L) }
    }

    @Test
    fun `delete wraps repository exception as failure`() = runTest {
        coEvery { repository.deleteExpense(any()) } throws RuntimeException("DB error")
        val result = useCase(1L)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message == "DB error")
    }
}
