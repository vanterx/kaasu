package com.example.expense.ui.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Expenses : Screen("expenses", "Expenses", Icons.AutoMirrored.Filled.ListAlt)
    data object Charts : Screen("charts", "Reports", Icons.Default.BarChart)
    data object Export : Screen("export", "Export", Icons.Default.FileDownload)
    data object AddExpense : Screen("add_expense", "", Icons.Default.Edit)
    data object EditExpense : Screen("edit_expense", "", Icons.Default.Edit)
    data object Settings : Screen("settings", "", Icons.Default.Settings)
}

private val mainScreens = listOf(Screen.Expenses, Screen.Charts, Screen.Export)

@Composable
fun ExpenseNavGraph(dataModule: DataModule) {
    val navController = rememberNavController()
    val currencyCode by dataModule.preferencesManager.currencyCode.collectAsState(initial = "NZD")
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
    val currentRoute = navBackStackEntry?.destination?.route

    val openDrawer: () -> Unit = { scope.launch { drawerState.open() } }

    fun navigateTo(route: String) {
        scope.launch { drawerState.close() }
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                currentRoute = currentRoute,
                onNavigate = ::navigateTo,
                onSettings = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
    ) {
        Scaffold { innerPadding ->
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
                        onOpenDrawer = openDrawer
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
                    ChartScreen(
                        viewModel = chartViewModel,
                        currencyCode = currencyCode,
                        onOpenDrawer = openDrawer
                    )
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
                    ExportScreen(viewModel = exportViewModel, onOpenDrawer = openDrawer)
                }
            }
        }
    }
}

@Composable
private fun DrawerContent(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onSettings: () -> Unit
) {
    ModalDrawerSheet {
        Text(
            "Kaasu",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        mainScreens.forEach { screen ->
            NavigationDrawerItem(
                icon = { Icon(screen.icon, screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = { onNavigate(screen.route) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }

        Spacer(Modifier.weight(1f))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, "Settings") },
            label = { Text("Settings") },
            selected = currentRoute == Screen.Settings.route,
            onClick = onSettings,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        Spacer(Modifier.height(8.dp))
    }
}
