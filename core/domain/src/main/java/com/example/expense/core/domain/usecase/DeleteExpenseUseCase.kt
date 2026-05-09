package com.example.expense.core.domain.usecase

import com.example.expense.core.domain.repository.ExpenseRepository

class DeleteExpenseUseCase(private val repository: ExpenseRepository) {
    suspend operator fun invoke(id: Long): Result<Unit> = runCatching {
        repository.deleteExpense(id)
    }
}
