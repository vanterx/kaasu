package com.example.expense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.expense.ui.navigation.ExpenseNavGraph
import com.example.expense.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as ExpenseApp
            ExpenseTrackerTheme {
                ExpenseNavGraph(
                    expenseRepository = app.expenseRepository,
                    categoryRepository = app.categoryRepository,
                    preferencesManager = app.preferencesManager
                )
            }
        }
    }
}
