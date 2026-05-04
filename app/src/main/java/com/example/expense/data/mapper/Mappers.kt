package com.example.expense.data.mapper

import com.example.expense.data.model.Category
import com.example.expense.data.model.Expense
import com.example.expense.data.model.ExpenseDisplay
import com.example.expense.data.model.ExpenseWithCategory

fun ExpenseWithCategory.toDisplay(): ExpenseDisplay {
    return ExpenseDisplay(
        id = expense.id,
        amount = expense.amount,
        description = expense.description,
        dateMillis = expense.dateMillis,
        account = expense.account,
        categoryId = expense.categoryId,
        categoryName = category?.name,
        categoryColorIndex = category?.colorIndex
    )
}

fun List<ExpenseWithCategory>.toDisplay(): List<ExpenseDisplay> {
    return map { it.toDisplay() }
}

fun ExpenseDisplay.toEntity(): Expense {
    return Expense(
        id = id,
        amount = amount,
        description = description,
        dateMillis = dateMillis,
        account = account,
        categoryId = categoryId
    )
}
