package com.example.expense

import android.app.Application
import com.example.expense.data.db.ExpenseDatabase
import com.example.expense.data.repository.CategoryRepository
import com.example.expense.data.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ExpenseApp : Application() {

    val database by lazy { ExpenseDatabase.create(this) }
    val expenseRepository by lazy { ExpenseRepository(database.expenseDao()) }
    val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            categoryRepository.seedDefaultCategories()
        }
    }
}
