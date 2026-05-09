package com.example.expense.core.data

import android.content.Context
import com.example.expense.core.data.repository.CategoryRepositoryImpl
import com.example.expense.core.data.repository.ExpenseRepositoryImpl
import com.example.expense.core.database.ExpenseDatabase
import com.example.expense.core.domain.repository.CategoryRepository
import com.example.expense.core.domain.repository.ExpenseRepository
import com.example.expense.core.domain.usecase.DeleteExpenseUseCase
import com.example.expense.core.domain.usecase.GetFilteredExpensesUseCase
import com.example.expense.core.domain.usecase.SaveExpenseUseCase

/**
 * Public DI container for the data layer.
 * Only :app depends on this; feature modules depend on :core:domain interfaces only.
 */
class DataModule(context: Context) {

    private val database: ExpenseDatabase = ExpenseDatabase.create(context)

    val expenseRepository: ExpenseRepository =
        ExpenseRepositoryImpl(database.expenseDao())

    val categoryRepository: CategoryRepository =
        CategoryRepositoryImpl(database.categoryDao())

    val preferencesManager: PreferencesManager = PreferencesManager(context)

    val getFilteredExpensesUseCase: GetFilteredExpensesUseCase =
        GetFilteredExpensesUseCase(expenseRepository)

    val saveExpenseUseCase: SaveExpenseUseCase =
        SaveExpenseUseCase(expenseRepository)

    val deleteExpenseUseCase: DeleteExpenseUseCase =
        DeleteExpenseUseCase(expenseRepository)
}
