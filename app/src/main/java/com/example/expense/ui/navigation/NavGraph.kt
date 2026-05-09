package com.example.expense.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.expense.core.data.DataModule
import com.example.expense.ui.category.CategoryViewModel
import com.example.expense.ui.chart.ChartScreen
import com.example.expense.ui.chart.ChartViewModel
import com.example.expense.ui.expense.AddEditExpenseScreen
import com.example.expense.ui.expense.ExpenseListScreen
import com.example.expense.ui.expense.ExpenseViewModel
import com.example.expense.ui.export.ExportScreen
import com.example.expense.ui.export.ExportViewModel
import com.example.expense.ui.settings.SettingsScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Expenses : Screen("expenses", "Expenses", Icons.AutoMirrored.Filled.ListAlt)
    data object Charts : Screen("charts", "Reports", Icons.Default.BarChart)
    data object Export : Screen("export", "Export", Icons.Default.FileDownload)
    data object AddExpense : Screen("add_expense", "", Icons.Default.Edit)
    data object EditExpense : Screen("edit_expense", "", Icons.Default.Edit)
    data object Settings : Screen("settings", "", Icons.Default.Settings)
}

private val bottomNavScreens = listOf(Screen.Expenses, Screen.Charts, Screen.Export)

@Composable
fun ExpenseNavGraph(dataModule: DataModule) {
    val navController = rememberNavController()
    val currencyCode by dataModule.preferencesManager.currencyCode.collectAsState(initial = "NZD")

    val expenseViewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModel.Factory(
            dataModule.expenseRepository,
            dataModule.categoryRepository,
            dataModule.getFilteredExpensesUseCase,
            dataModule.saveExpenseUseCase,
            dataModule.deleteExpenseUseCase
        )
    )
    val chartViewModel: ChartViewModel = viewModel(
        factory = ChartViewModel.Factory(dataModule.expenseRepository)
    )
    val categoryViewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModel.Factory(dataModule.categoryRepository)
    )
    val exportViewModel: ExportViewModel = viewModel(
        factory = ExportViewModel.Factory(dataModule.expenseRepository)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = bottomNavScreens.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
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
                    onAddExpense = { navController.navigate(Screen.AddExpense.route) },
                    onEditExpense = { expense ->
                        expenseViewModel.setEditingExpense(expense)
                        navController.navigate(Screen.EditExpense.route)
                    },
                    onOpenSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.AddExpense.route) {
                AddEditExpenseScreen(
                    viewModel = expenseViewModel,
                    currencyCode = currencyCode,
                    onNavigateBack = {
                        expenseViewModel.setEditingExpense(null)
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.EditExpense.route) {
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
                ChartScreen(viewModel = chartViewModel, currencyCode = currencyCode)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    categoryViewModel = categoryViewModel,
                    preferencesManager = dataModule.preferencesManager,
                    currencyCode = currencyCode,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Export.route) {
                ExportScreen(viewModel = exportViewModel)
            }
        }
    }
}
