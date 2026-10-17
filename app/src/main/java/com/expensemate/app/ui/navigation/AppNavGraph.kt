package com.expensemate.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.expensemate.app.data.repository.ExpenseMateRepository
import com.expensemate.app.ui.accounts.AccountsScreen
import com.expensemate.app.ui.accounts.AccountsViewModel
import com.expensemate.app.ui.budgets.BudgetsScreen
import com.expensemate.app.ui.budgets.BudgetsViewModel
import com.expensemate.app.ui.categories.CategoriesScreen
import com.expensemate.app.ui.categories.CategoriesViewModel
import com.expensemate.app.ui.dashboard.DashboardScreen
import com.expensemate.app.ui.dashboard.DashboardViewModel
import com.expensemate.app.ui.reports.ReportsScreen
import com.expensemate.app.ui.reports.ReportsViewModel
import com.expensemate.app.ui.settings.SettingsScreen
import com.expensemate.app.ui.settings.SettingsViewModel
import com.expensemate.app.ui.transactions.AddEditTransactionScreen
import com.expensemate.app.ui.transactions.AddEditTransactionViewModel
import com.expensemate.app.ui.transactions.TransactionsScreen
import com.expensemate.app.ui.transactions.TransactionsViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    repository: ExpenseMateRepository,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        // Dashboard Screen
        composable(Screen.Dashboard.route) {
            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModel.Factory(repository)
            )
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToAddTransaction = { type ->
                    navController.navigate(Screen.AddTransaction.createRoute(type))
                },
                onNavigateToEditTransaction = { txId ->
                    navController.navigate(Screen.EditTransaction.createRoute(txId))
                },
                onNavigateToTransactions = {
                    navController.navigate(Screen.Transactions.route)
                },
                onNavigateToBudgets = {
                    navController.navigate(Screen.Budgets.route)
                },
                onNavigateToAccounts = {
                    navController.navigate(Screen.Accounts.route)
                }
            )
        }

        // Transactions History Screen
        composable(Screen.Transactions.route) {
            val transactionsViewModel: TransactionsViewModel = viewModel(
                factory = TransactionsViewModel.Factory(repository)
            )
            TransactionsScreen(
                viewModel = transactionsViewModel,
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction.createRoute("EXPENSE"))
                },
                onNavigateToEditTransaction = { txId ->
                    navController.navigate(Screen.EditTransaction.createRoute(txId))
                }
            )
        }

        // Add Transaction Screen
        composable(
            route = Screen.AddTransaction.route,
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = "EXPENSE"
                }
            )
        ) { backStackEntry ->
            val addEditViewModel: AddEditTransactionViewModel = viewModel(
                factory = AddEditTransactionViewModel.Factory(repository, backStackEntry.savedStateHandle)
            )
            AddEditTransactionScreen(
                viewModel = addEditViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Edit Transaction Screen
        composable(
            route = Screen.EditTransaction.route,
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val addEditViewModel: AddEditTransactionViewModel = viewModel(
                factory = AddEditTransactionViewModel.Factory(repository, backStackEntry.savedStateHandle)
            )
            AddEditTransactionScreen(
                viewModel = addEditViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Budgets Screen
        composable(Screen.Budgets.route) {
            val budgetsViewModel: BudgetsViewModel = viewModel(
                factory = BudgetsViewModel.Factory(repository)
            )
            BudgetsScreen(viewModel = budgetsViewModel)
        }

        // Reports Screen
        composable(Screen.Reports.route) {
            val reportsViewModel: ReportsViewModel = viewModel(
                factory = ReportsViewModel.Factory(repository)
            )
            ReportsScreen(viewModel = reportsViewModel)
        }

        // Accounts Screen
        composable(Screen.Accounts.route) {
            val accountsViewModel: AccountsViewModel = viewModel(
                factory = AccountsViewModel.Factory(repository)
            )
            AccountsScreen(
                viewModel = accountsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Categories Screen
        composable(Screen.Categories.route) {
            val categoriesViewModel: CategoriesViewModel = viewModel(
                factory = CategoriesViewModel.Factory(repository)
            )
            CategoriesScreen(
                viewModel = categoriesViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(repository)
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                onNavigateToAccounts = { navController.navigate(Screen.Accounts.route) }
            )
        }
    }
}
