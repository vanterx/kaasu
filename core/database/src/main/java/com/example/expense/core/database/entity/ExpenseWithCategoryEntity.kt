package com.example.expense.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ExpenseWithCategoryEntity(
    @Embedded val expense: ExpenseEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)
