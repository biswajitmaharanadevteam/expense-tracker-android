package com.expensemate.app.data.repository

import com.expensemate.app.data.local.ExpenseMateDatabase
import com.expensemate.app.data.local.entity.AccountEntity
import com.expensemate.app.data.local.entity.BudgetEntity
import com.expensemate.app.data.local.entity.CategoryEntity
import com.expensemate.app.data.local.entity.RecurringRuleEntity
import com.expensemate.app.data.local.entity.TransactionEntity
import com.expensemate.app.data.preferences.UserPreferencesManager
import com.expensemate.app.domain.model.Account
import com.expensemate.app.domain.model.AppSettings
import com.expensemate.app.domain.model.Budget
import com.expensemate.app.domain.model.Category
import com.expensemate.app.domain.model.CategorySpending
import com.expensemate.app.domain.model.CurrencyConfig
import com.expensemate.app.domain.model.DateFormatOption
import com.expensemate.app.domain.model.FirstDayOfWeek
import com.expensemate.app.domain.model.PaymentMethod
import com.expensemate.app.domain.model.RecurringRule
import com.expensemate.app.domain.model.ThemeMode
import com.expensemate.app.domain.model.Transaction
import com.expensemate.app.domain.model.TransactionType
import com.expensemate.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ExpenseMateRepositoryImpl(
    private val database: ExpenseMateDatabase,
    private val preferencesManager: UserPreferencesManager
) : ExpenseMateRepository {

    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val accountDao = database.accountDao()
    private val budgetDao = database.budgetDao()
    private val recurringRuleDao = database.recurringRuleDao()

    // ----------------- TRANSACTIONS -----------------

    override fun getAllTransactionsFlow(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactionsWithDetailsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTransactionsBetweenDatesFlow(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsWithDetailsBetweenDatesFlow(startDate, endDate).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun searchTransactionsFlow(query: String): Flow<List<Transaction>> {
        return transactionDao.searchTransactionsFlow(query).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        val entity = transactionDao.getTransactionById(id) ?: return null
        val category = categoryDao.getCategoryById(entity.categoryId)
        val account = accountDao.getAccountById(entity.accountId)
        return entity.toDomain(
            categoryName = category?.name ?: "Unknown",
            categoryIcon = category?.iconName ?: "category",
            categoryColor = category?.colorHex ?: "#2A3B90",
            accountName = account?.name ?: "Account"
        )
    }

    override suspend fun insertTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(TransactionEntity.fromDomain(transaction))
    }

    override suspend fun insertTransactions(transactions: List<Transaction>): List<Long> {
        return transactionDao.insertTransactions(transactions.map { TransactionEntity.fromDomain(it) })
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(TransactionEntity.fromDomain(transaction))
    }

    override suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteById(id)
    }

    override fun getTotalExpenseFlow(startDate: Long, endDate: Long): Flow<Double> {
        return transactionDao.getTotalExpenseBetweenDatesFlow(startDate, endDate)
    }

    override fun getTotalIncomeFlow(startDate: Long, endDate: Long): Flow<Double> {
        return transactionDao.getTotalIncomeBetweenDatesFlow(startDate, endDate)
    }

    override fun getCategoryExpenseSumsFlow(startDate: Long, endDate: Long): Flow<List<CategorySpending>> {
        return combine(
            transactionDao.getCategoryExpenseSumsBetweenDatesFlow(startDate, endDate),
            transactionDao.getTotalExpenseBetweenDatesFlow(startDate, endDate)
        ) { categorySums, totalExpense ->
            categorySums.map { sum ->
                val percentage = if (totalExpense > 0) ((sum.totalAmount / totalExpense) * 100).toFloat() else 0f
                CategorySpending(
                    categoryId = sum.categoryId,
                    categoryName = sum.categoryName,
                    colorHex = sum.colorHex,
                    iconName = sum.iconName,
                    totalAmount = sum.totalAmount,
                    percentage = percentage
                )
            }
        }
    }

    // ----------------- CATEGORIES -----------------

    override fun getAllCategoriesFlow(): Flow<List<Category>> {
        return categoryDao.getAllCategoriesFlow().map { list -> list.map { it.toDomain() } }
    }

    override fun getCategoriesByTypeFlow(type: TransactionType): Flow<List<Category>> {
        return categoryDao.getCategoriesByTypeFlow(type.name).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getCategoryById(id: Long): Category? {
        return categoryDao.getCategoryById(id)?.toDomain()
    }

    override suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(CategoryEntity.fromDomain(category))
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(CategoryEntity.fromDomain(category))
    }

    override suspend fun deleteCategory(id: Long) {
        categoryDao.deleteById(id)
    }

    override suspend fun canDeleteCategory(categoryId: Long): Boolean {
        return transactionDao.getCountByCategoryId(categoryId) == 0
    }

    // ----------------- ACCOUNTS -----------------

    override fun getAccountsWithBalancesFlow(): Flow<List<Account>> {
        return accountDao.getAccountsWithComputedBalancesFlow().map { list ->
            list.map { item ->
                Account(
                    id = item.accountId,
                    name = item.name,
                    type = try {
                        com.expensemate.app.domain.model.AccountType.valueOf(item.type)
                    } catch (e: Exception) {
                        com.expensemate.app.domain.model.AccountType.CASH
                    },
                    initialBalance = item.initialBalance,
                    currentBalance = item.currentBalance,
                    colorHex = item.colorHex,
                    isDefault = item.isDefault
                )
            }
        }
    }

    override fun getAllAccountsFlow(): Flow<List<Account>> {
        return accountDao.getAllAccountsFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getAccountById(id: Long): Account? {
        return accountDao.getAccountById(id)?.toDomain()
    }

    override suspend fun insertAccount(account: Account): Long {
        return accountDao.insertAccount(AccountEntity.fromDomain(account))
    }

    override suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(AccountEntity.fromDomain(account))
    }

    override suspend fun deleteAccount(id: Long) {
        accountDao.deleteById(id)
    }

    override suspend fun canDeleteAccount(accountId: Long): Boolean {
        return transactionDao.getCountByAccountId(accountId) == 0
    }

    // ----------------- BUDGETS -----------------

    override fun getBudgetsForMonthFlow(month: Int, year: Int): Flow<List<Budget>> {
        val (monthStart, monthEnd) = DateUtils.getMonthStartAndEnd(month, year)

        return combine(
            budgetDao.getBudgetsForMonthFlow(month, year),
            transactionDao.getTransactionsWithDetailsBetweenDatesFlow(monthStart, monthEnd)
        ) { rawBudgets, transactions ->
            rawBudgets.map { b ->
                val spent = transactions.filter {
                    it.type == TransactionType.EXPENSE.name && (b.categoryId == null || it.categoryId == b.categoryId)
                }.sumOf { it.amount }

                Budget(
                    id = b.id,
                    categoryId = b.categoryId,
                    categoryName = b.categoryName ?: "Overall Budget",
                    categoryIcon = b.categoryIcon ?: "account_balance_wallet",
                    categoryColor = b.categoryColor ?: "#2A3B90",
                    month = b.month,
                    year = b.year,
                    amountLimit = b.amountLimit,
                    amountSpent = spent
                )
            }
        }
    }

    override suspend fun getBudgetById(id: Long): Budget? {
        val entity = budgetDao.getBudgetById(id) ?: return null
        val category = entity.categoryId?.let { categoryDao.getCategoryById(it) }
        val (monthStart, monthEnd) = DateUtils.getMonthStartAndEnd(entity.month, entity.year)
        val spent = transactionDao.getSpentAmountForBudget(entity.categoryId, monthStart, monthEnd)
        return entity.toDomain(
            categoryName = category?.name ?: "Overall Budget",
            categoryIcon = category?.iconName ?: "account_balance_wallet",
            categoryColor = category?.colorHex ?: "#2A3B90",
            amountSpent = spent
        )
    }

    override suspend fun insertBudget(budget: Budget): Long {
        return budgetDao.insertBudget(BudgetEntity.fromDomain(budget))
    }

    override suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(BudgetEntity.fromDomain(budget))
    }

    override suspend fun deleteBudget(id: Long) {
        budgetDao.deleteById(id)
    }

    // ----------------- RECURRING RULES -----------------

    override fun getAllRecurringRulesFlow(): Flow<List<RecurringRule>> {
        return recurringRuleDao.getAllRulesWithDetailsFlow().map { list ->
            list.map { r ->
                RecurringRule(
                    id = r.id,
                    title = r.title,
                    amount = r.amount,
                    type = if (r.type == TransactionType.INCOME.name) TransactionType.INCOME else TransactionType.EXPENSE,
                    categoryId = r.categoryId,
                    categoryName = r.categoryName ?: "",
                    accountId = r.accountId,
                    accountName = r.accountName ?: "",
                    paymentMethod = try {
                        PaymentMethod.valueOf(r.paymentMethod)
                    } catch (e: Exception) {
                        PaymentMethod.CASH
                    },
                    frequency = try {
                        com.expensemate.app.domain.model.RecurrenceFrequency.valueOf(r.frequency)
                    } catch (e: Exception) {
                        com.expensemate.app.domain.model.RecurrenceFrequency.MONTHLY
                    },
                    startDate = r.startDate,
                    endDate = r.endDate,
                    lastExecutedDate = r.lastExecutedDate,
                    isActive = r.isActive,
                    reminderEnabled = r.reminderEnabled
                )
            }
        }
    }

    override suspend fun getActiveRecurringRules(): List<RecurringRule> {
        return recurringRuleDao.getActiveRules().map { r ->
            val cat = categoryDao.getCategoryById(r.categoryId)
            val acc = accountDao.getAccountById(r.accountId)
            r.toDomain(categoryName = cat?.name ?: "", accountName = acc?.name ?: "")
        }
    }

    override suspend fun getRecurringRuleById(id: Long): RecurringRule? {
        val r = recurringRuleDao.getRuleById(id) ?: return null
        val cat = categoryDao.getCategoryById(r.categoryId)
        val acc = accountDao.getAccountById(r.accountId)
        return r.toDomain(categoryName = cat?.name ?: "", accountName = acc?.name ?: "")
    }

    override suspend fun insertRecurringRule(rule: RecurringRule): Long {
        return recurringRuleDao.insertRule(RecurringRuleEntity.fromDomain(rule))
    }

    override suspend fun updateRecurringRule(rule: RecurringRule) {
        recurringRuleDao.updateRule(RecurringRuleEntity.fromDomain(rule))
    }

    override suspend fun deleteRecurringRule(id: Long) {
        recurringRuleDao.deleteById(id)
    }

    // ----------------- PREFERENCES -----------------

    override val appSettingsFlow: Flow<AppSettings> = preferencesManager.appSettingsFlow

    override suspend fun setThemeMode(mode: ThemeMode) {
        preferencesManager.setThemeMode(mode)
    }

    override suspend fun setCurrency(currency: CurrencyConfig) {
        preferencesManager.setCurrency(currency)
    }

    override suspend fun setFirstDayOfWeek(firstDay: FirstDayOfWeek) {
        preferencesManager.setFirstDayOfWeek(firstDay)
    }

    override suspend fun setDateFormat(format: DateFormatOption) {
        preferencesManager.setDateFormat(format)
    }

    override suspend fun setDailyReminder(enabled: Boolean, hour: Int, minute: Int) {
        preferencesManager.setDailyReminder(enabled, hour, minute)
    }

    override suspend fun setBudgetAlertsEnabled(enabled: Boolean) {
        preferencesManager.setBudgetAlertsEnabled(enabled)
    }

    override suspend fun setRecurringAlertsEnabled(enabled: Boolean) {
        preferencesManager.setRecurringAlertsEnabled(enabled)
    }

    // ----------------- RESET -----------------

    override suspend fun resetAllData() {
        transactionDao.deleteAll()
        budgetDao.deleteAll()
        recurringRuleDao.deleteAll()
        categoryDao.deleteAll()
        accountDao.deleteAll()

        // Re-populate default data
        val initialCategories = (Category.DEFAULT_EXPENSE_CATEGORIES + Category.DEFAULT_INCOME_CATEGORIES)
            .map { CategoryEntity.fromDomain(it) }
        categoryDao.insertCategories(initialCategories)

        val initialAccounts = Account.DEFAULT_ACCOUNTS
            .map { AccountEntity.fromDomain(it) }
        accountDao.insertAccounts(initialAccounts)

        preferencesManager.clearAllPreferences()
    }
}

private fun com.expensemate.app.data.local.dao.TransactionWithDetails.toDomain(): Transaction {
    return Transaction(
        id = id,
        title = title,
        amount = amount,
        type = if (type == TransactionType.INCOME.name) TransactionType.INCOME else TransactionType.EXPENSE,
        categoryId = categoryId,
        categoryName = categoryName,
        categoryIcon = categoryIcon,
        categoryColor = categoryColor,
        accountId = accountId,
        accountName = accountName,
        paymentMethod = try {
            PaymentMethod.valueOf(paymentMethod)
        } catch (e: Exception) {
            PaymentMethod.CASH
        },
        date = date,
        payeeOrMerchant = payeeOrMerchant,
        notes = notes,
        tags = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() },
        isRecurring = isRecurring,
        recurringRuleId = recurringRuleId
    )
}
