package com.example.expense

import android.app.Application
import com.example.expense.core.data.DataModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ExpenseApp : Application() {

    val dataModule by lazy { DataModule(this) }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            dataModule.categoryRepository.seedDefaultCategories()
        }
    }
}
