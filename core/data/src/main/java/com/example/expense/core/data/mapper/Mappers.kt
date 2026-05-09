package com.example.expense.core.data.mapper

import com.example.expense.core.database.entity.CategoryEntity
import com.example.expense.core.database.entity.ExpenseWithCategoryEntity
import com.example.expense.core.domain.model.Category
import com.example.expense.core.domain.model.ExpenseItem

internal fun ExpenseWithCategoryEntity.toDomain(): ExpenseItem = ExpenseItem(
    id = expense.id,
    amount = expense.amount,
    description = expense.description,
    dateMillis = expense.dateMillis,
    account = expense.account,
    categoryId = expense.categoryId,
    categoryName = category?.name,
    categoryColorIndex = category?.colorIndex
)

internal fun List<ExpenseWithCategoryEntity>.toDomain(): List<ExpenseItem> = map { it.toDomain() }

internal fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    colorIndex = colorIndex
)

internal fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    colorIndex = colorIndex
)
