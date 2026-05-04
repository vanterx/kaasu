package com.example.expense.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.expense.data.repository.CategoryRepository
import com.example.expense.data.repository.ExpenseRepository
import com.example.expense.ui.category.CategoryScreen
import com.example.expense.ui.category.CategoryViewModel
import com.example.expense.ui.chart.ChartScreen
import com.example.expense.ui.chart.ChartViewModel
import com.example.expense.ui.expense.AddEditExpenseScreen
import com.example.expense.ui.expense.CurrencyPickerDialog
import com.example.expense.ui.expense.ExpenseListScreen
import com.example.expense.ui.expense.ExpenseViewModel
import com.example.expense.ui.export.ExportScreen
import com.example.expense.util.PreferencesManager
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Expenses : Screen("expenses", "Expenses", Icons.AutoMirrored.Filled.ListAlt)
    data object Charts : Screen("charts", "Reports", Icons.Default.BarChart)
    data object Categories : Screen("categories", "Categories", Icons.Default.Category)
    data object Export : Screen("export", "Export", Icons.Default.FileDownload)
}

private val bottomNavScreens = listOf(
    Screen.Expenses,
    Screen.Charts,
    Screen.Categories,
    Screen.Export
)

@Composable
fun ExpenseNavGraph(
    expenseRepository: ExpenseRepository,
    categoryRepository: CategoryRepository,
    preferencesManager: PreferencesManager
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val currencyCode by preferencesManager.currencyCode.collectAsState(initial = "NZD")
    var showCurrencyPicker by remember { mutableStateOf(false) }

    val expenseViewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModel.Factory(expenseRepository, categoryRepository)
    )
    val chartViewModel: ChartViewModel = viewModel(
        factory = ChartViewModel.Factory(expenseRepository)
    )
    val categoryViewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModel.Factory(categoryRepository)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = bottomNavScreens.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            currentCode = currencyCode,
            onDismiss = { showCurrencyPicker = false },
            onSelect = { code ->
                scope.launch {
                    preferencesManager.setCurrencyCode(code)
                }
                showCurrencyPicker = false
            }
        )
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any {
                                it.route == screen.route
                            } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Expenses.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Expenses.route) {
                ExpenseListScreen(
                    viewModel = expenseViewModel,
                    currencyCode = currencyCode,
                    onAddExpense = { navController.navigate("add_expense") },
                    onEditExpense = { expense ->
                        expenseViewModel.setEditingExpense(expense)
                        navController.navigate("edit_expense")
                    },
                    onOpenCurrencyPicker = { showCurrencyPicker = true }
                )
            }

            composable("add_expense") {
                AddEditExpenseScreen(
                    viewModel = expenseViewModel,
                    currencyCode = currencyCode,
                    onNavigateBack = {
                        expenseViewModel.setEditingExpense(null)
                        navController.popBackStack()
                    }
                )
            }

            composable("edit_expense") {
                AddEditExpenseScreen(
                    viewModel = expenseViewModel,
                    currencyCode = currencyCode,
                    onNavigateBack = {
                        expenseViewModel.setEditingExpense(null)
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Charts.route) {
                ChartScreen(
                    viewModel = chartViewModel,
                    currencyCode = currencyCode
                )
            }

            composable(Screen.Categories.route) {
                CategoryScreen(viewModel = categoryViewModel)
            }

            composable(Screen.Export.route) {
                ExportScreen(expenseRepository = expenseRepository)
            }
        }
    }
}
