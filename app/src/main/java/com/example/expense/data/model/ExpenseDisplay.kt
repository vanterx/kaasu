package com.example.expense.data.model

data class ExpenseDisplay(
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val dateMillis: Long,
    val account: String? = null,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val categoryColorIndex: Int? = null
)
