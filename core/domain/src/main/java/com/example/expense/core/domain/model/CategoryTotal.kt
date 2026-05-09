package com.example.expense.core.domain.model

data class CategoryTotal(
    val id: Long,
    val name: String,
    val colorIndex: Int,
    val total: Double?
)
