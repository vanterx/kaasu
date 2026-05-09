package com.example.expense.core.domain.usecase

import com.example.expense.core.domain.repository.ExpenseRepository

class SaveExpenseUseCase(private val repository: ExpenseRepository) {

    suspend fun save(
        amount: Double,
        description: String,
        categoryId: Long?,
        dateMillis: Long,
        account: String?
    ): Result<Long> = runCatching {
        require(amount > 0) { "Amount must be positive" }
        repository.saveExpense(amount, description.trim(), categoryId, dateMillis, account)
    }

    suspend fun update(
        id: Long,
        amount: Double,
        description: String,
        categoryId: Long?,
        dateMillis: Long,
        account: String?
    ): Result<Unit> = runCatching {
        require(amount > 0) { "Amount must be positive" }
        repository.updateExpense(id, amount, description.trim(), categoryId, dateMillis, account)
    }
}
