package com.example.expense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.expense.ui.navigation.ExpenseNavGraph
import com.example.expense.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as ExpenseApp
            val themeMode by app.dataModule.preferencesManager.themeMode
                .collectAsState(initial = "SYSTEM")
            ExpenseTrackerTheme(themeMode = themeMode) {
                ExpenseNavGraph(dataModule = app.dataModule)
            }
        }
    }
}
