package com.expensemate.app.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Transactions : Screen("transactions")
    object AddTransaction : Screen("transaction/add?type={type}") {
        fun createRoute(type: String = "EXPENSE") = "transaction/add?type=$type"
    }
    object EditTransaction : Screen("transaction/edit/{transactionId}") {
        fun createRoute(transactionId: Long) = "transaction/edit/$transactionId"
    }
    object Budgets : Screen("budgets")
    object Reports : Screen("reports")
    object Accounts : Screen("accounts")
    object Categories : Screen("categories")
    object Settings : Screen("settings")
}
