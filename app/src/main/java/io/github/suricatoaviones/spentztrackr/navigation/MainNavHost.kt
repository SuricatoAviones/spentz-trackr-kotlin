package io.github.suricatoaviones.spentztrackr.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.suricatoaviones.spentztrackr.R
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryType
import io.github.suricatoaviones.spentztrackr.feature.catalog.CategoriesScreen
import io.github.suricatoaviones.spentztrackr.feature.catalog.CategoryFormScreen
import io.github.suricatoaviones.spentztrackr.feature.catalog.SourcesScreen
import io.github.suricatoaviones.spentztrackr.feature.expenses.ExpenseDetailScreen
import io.github.suricatoaviones.spentztrackr.feature.expenses.ExpenseFormScreen
import io.github.suricatoaviones.spentztrackr.feature.expenses.ExpensesScreen
import io.github.suricatoaviones.spentztrackr.feature.home.HomeScreen
import io.github.suricatoaviones.spentztrackr.feature.incomes.IncomeDetailScreen
import io.github.suricatoaviones.spentztrackr.feature.incomes.IncomeFormScreen
import io.github.suricatoaviones.spentztrackr.feature.incomes.IncomesScreen
import io.github.suricatoaviones.spentztrackr.feature.rates.RatesScreen
import io.github.suricatoaviones.spentztrackr.feature.settings.SettingsScreen

/**
 * App principal tras el onboarding. La pestaña Ingresos solo aparece si el
 * usuario registra ingresos (`tracking_type` del backend, docs/04).
 */
@Composable
fun MainNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val tabs =
        listOf(
            Tab(Routes.HOME, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
            Tab(Routes.EXPENSES, R.string.nav_expenses, Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
            Tab(Routes.INCOMES, R.string.nav_incomes, Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
            Tab(Routes.SETTINGS, R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings),
        )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val selected =
                        currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = { navController.navigateToTab(tab.route) },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.iconSelected else tab.iconUnselected,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(tab.labelRes)) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(onAddExpense = { navController.navigate(Routes.expenseForm()) })
            }
            composable(Routes.EXPENSES) {
                ExpensesScreen(
                    onAddExpense = { navController.navigate(Routes.expenseForm()) },
                    onOpenExpense = { id -> navController.navigate(Routes.expenseDetail(id)) },
                )
            }
            composable(Routes.INCOMES) {
                IncomesScreen(
                    onAddIncome = { navController.navigate(Routes.incomeForm()) },
                    onOpenIncome = { id -> navController.navigate(Routes.incomeDetail(id)) },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onConnectServer = { navController.navigate(Routes.serverLogin(fromSettings = true)) },
                    onOpenCatalogs = { navController.navigate(Routes.CATEGORIES) },
                    onOpenSources = { navController.navigate(Routes.SOURCES) },
                    onOpenRates = { navController.navigate(Routes.RATES) },
                )
            }

            composable(
                route = Routes.EXPENSE_FORM,
                arguments =
                    listOf(
                        navArgument(Routes.ARG_EDITING) {
                            type = NavType.StringType
                            defaultValue = ""
                        },
                    ),
            ) { entry ->
                ExpenseFormScreen(
                    editingId =
                        entry.arguments
                            ?.getString(Routes.ARG_EDITING)
                            .orEmpty()
                            .toLongOrNull(),
                    onSaved = { navController.popBackStack() },
                )
            }

            composable(
                route = Routes.EXPENSE_DETAIL,
                arguments =
                    listOf(
                        navArgument(Routes.ARG_EXPENSE_ID) { type = NavType.LongType },
                    ),
            ) { entry ->
                ExpenseDetailScreen(
                    expenseId = entry.arguments?.getLong(Routes.ARG_EXPENSE_ID) ?: return@composable,
                    onBack = { navController.popBackStack() },
                    onEdit = { editing -> navController.navigate(Routes.expenseForm(editing)) },
                )
            }

            composable(
                route = Routes.INCOME_FORM,
                arguments =
                    listOf(
                        navArgument(Routes.ARG_EDITING) {
                            type = NavType.StringType
                            defaultValue = ""
                        },
                    ),
            ) { entry ->
                IncomeFormScreen(
                    editingId =
                        entry.arguments
                            ?.getString(Routes.ARG_EDITING)
                            .orEmpty()
                            .toLongOrNull(),
                    onSaved = { navController.popBackStack() },
                )
            }

            composable(
                route = Routes.INCOME_DETAIL,
                arguments =
                    listOf(
                        navArgument(Routes.ARG_EXPENSE_ID) { type = NavType.LongType },
                    ),
            ) { entry ->
                IncomeDetailScreen(
                    incomeId = entry.arguments?.getLong(Routes.ARG_EXPENSE_ID) ?: return@composable,
                    onBack = { navController.popBackStack() },
                    onEdit = { editing -> navController.navigate(Routes.incomeForm(editing)) },
                )
            }

            composable(Routes.CATEGORIES) {
                CategoriesScreen(
                    onNewCategory = { type ->
                        navController.navigate(Routes.categoryForm(type = type.value))
                    },
                    onEditCategory = { id -> navController.navigate(Routes.categoryForm(editing = id)) },
                )
            }

            composable(
                route = Routes.CATEGORY_FORM,
                arguments =
                    listOf(
                        navArgument(Routes.ARG_EDITING) {
                            type = NavType.StringType
                            defaultValue = ""
                        },
                        navArgument(Routes.ARG_CATEGORY_TYPE) {
                            type = NavType.StringType
                            defaultValue = "expense"
                        },
                    ),
            ) { entry ->
                val editingRaw = entry.arguments?.getString(Routes.ARG_EDITING).orEmpty()
                val typeRaw = entry.arguments?.getString(Routes.ARG_CATEGORY_TYPE).orEmpty()
                CategoryFormScreen(
                    editingId = editingRaw.toLongOrNull(),
                    defaultType = if (typeRaw == "income") CategoryType.INCOME else CategoryType.EXPENSE,
                    onSaved = { navController.popBackStack() },
                )
            }

            composable(Routes.SOURCES) {
                SourcesScreen()
            }

            composable(Routes.RATES) {
                RatesScreen(onBack = { navController.popBackStack() })
            }

            serverLoginDestination(navController)
        }
    }
}

private data class Tab(
    val route: String,
    val labelRes: Int,
    val iconSelected: androidx.compose.ui.graphics.vector.ImageVector,
    val iconUnselected: androidx.compose.ui.graphics.vector.ImageVector,
)

/** Patrón estándar de tabs: singleTop + estado salvado/restaurado. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
