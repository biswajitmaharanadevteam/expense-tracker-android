package com.expensemate.app.data.repository

import com.expensemate.app.domain.model.Account
import com.expensemate.app.domain.model.AppSettings
import com.expensemate.app.domain.model.Budget
import com.expensemate.app.domain.model.Category
import com.expensemate.app.domain.model.CategorySpending
import com.expensemate.app.domain.model.CurrencyConfig
import com.expensemate.app.domain.model.DateFormatOption
import com.expensemate.app.domain.model.FirstDayOfWeek
import com.expensemate.app.domain.model.RecurringRule
import com.expensemate.app.domain.model.ThemeMode
import com.expensemate.app.domain.model.Transaction
import com.expensemate.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface ExpenseMateRepository {

    // Transactions
    fun getAllTransactionsFlow(): Flow<List<Transaction>>
    fun getTransactionsBetweenDatesFlow(startDate: Long, endDate: Long): Flow<List<Transaction>>
    fun searchTransactionsFlow(query: String): Flow<List<Transaction>>
    suspend fun getTransactionById(id: Long): Transaction?
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun insertTransactions(transactions: List<Transaction>): List<Long>
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: Long)
    fun getTotalExpenseFlow(startDate: Long, endDate: Long): Flow<Double>
    fun getTotalIncomeFlow(startDate: Long, endDate: Long): Flow<Double>
    fun getCategoryExpenseSumsFlow(startDate: Long, endDate: Long): Flow<List<CategorySpending>>

    // Categories
    fun getAllCategoriesFlow(): Flow<List<Category>>
    fun getCategoriesByTypeFlow(type: TransactionType): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Category?
    suspend fun insertCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(id: Long)
    suspend fun canDeleteCategory(categoryId: Long): Boolean

    // Accounts
    fun getAccountsWithBalancesFlow(): Flow<List<Account>>
    fun getAllAccountsFlow(): Flow<List<Account>>
    suspend fun getAccountById(id: Long): Account?
    suspend fun insertAccount(account: Account): Long
    suspend fun updateAccount(account: Account)
    suspend fun deleteAccount(id: Long)
    suspend fun canDeleteAccount(accountId: Long): Boolean

    // Budgets
    fun getBudgetsForMonthFlow(month: Int, year: Int): Flow<List<Budget>>
    suspend fun getBudgetById(id: Long): Budget?
    suspend fun insertBudget(budget: Budget): Long
    suspend fun updateBudget(budget: Budget)
    suspend fun deleteBudget(id: Long)

    // Recurring Rules
    fun getAllRecurringRulesFlow(): Flow<List<RecurringRule>>
    suspend fun getActiveRecurringRules(): List<RecurringRule>
    suspend fun getRecurringRuleById(id: Long): RecurringRule?
    suspend fun insertRecurringRule(rule: RecurringRule): Long
    suspend fun updateRecurringRule(rule: RecurringRule)
    suspend fun deleteRecurringRule(id: Long)

    // Preferences & Settings
    val appSettingsFlow: Flow<AppSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setCurrency(currency: CurrencyConfig)
    suspend fun setFirstDayOfWeek(firstDay: FirstDayOfWeek)
    suspend fun setDateFormat(format: DateFormatOption)
    suspend fun setDailyReminder(enabled: Boolean, hour: Int, minute: Int)
    suspend fun setBudgetAlertsEnabled(enabled: Boolean)
    suspend fun setRecurringAlertsEnabled(enabled: Boolean)

    // Maintenance & Reset
    suspend fun resetAllData()
}
