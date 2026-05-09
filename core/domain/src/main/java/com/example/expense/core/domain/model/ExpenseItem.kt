package com.example.expense.core.domain.model

/** Domain representation of an expense, enriched with its category for display. */
data class ExpenseItem(
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val dateMillis: Long,
    val account: String? = null,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val categoryColorIndex: Int? = null
)
