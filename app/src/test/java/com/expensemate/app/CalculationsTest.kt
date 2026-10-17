package com.expensemate.app

import com.expensemate.app.domain.model.Account
import com.expensemate.app.domain.model.AccountType
import com.expensemate.app.domain.model.DateRange
import com.expensemate.app.domain.model.PaymentMethod
import com.expensemate.app.domain.model.Transaction
import com.expensemate.app.domain.model.TransactionType
import com.expensemate.app.domain.usecase.CalculateFinancialSummaryUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculationsTest {

    private val calculateSummaryUseCase = CalculateFinancialSummaryUseCase()

    @Test
    fun testFinancialSummaryCalculations() {
        val now = System.currentTimeMillis()
        val dateRange = DateRange(now - 7 * 24 * 3600 * 1000L, now + 1000L)

        val accounts = listOf(
            Account(id = 1, name = "Cash", type = AccountType.CASH, initialBalance = 1000.0, currentBalance = 3500.0),
            Account(id = 2, name = "Bank", type = AccountType.BANK, initialBalance = 5000.0, currentBalance = 8000.0)
        )

        val transactions = listOf(
            Transaction(
                id = 1,
                title = "Salary",
                amount = 10000.0,
                type = TransactionType.INCOME,
                categoryId = 101,
                categoryName = "Salary",
                accountId = 2,
                paymentMethod = PaymentMethod.BANK_ACCOUNT,
                date = now
            ),
            Transaction(
                id = 2,
                title = "Groceries",
                amount = 2500.0,
                type = TransactionType.EXPENSE,
                categoryId = 2,
                categoryName = "Groceries",
                accountId = 1,
                paymentMethod = PaymentMethod.CASH,
                date = now
            ),
            Transaction(
                id = 3,
                title = "Restaurant",
                amount = 1500.0,
                type = TransactionType.EXPENSE,
                categoryId = 1,
                categoryName = "Food and Dining",
                accountId = 1,
                paymentMethod = PaymentMethod.CASH,
                date = now
            )
        )

        val summary = calculateSummaryUseCase(
            periodTransactions = transactions,
            allAccounts = accounts,
            budgetsForPeriod = emptyList(),
            dateRange = dateRange
        )

        // Total Balance = 3500 + 8000 = 11500.0
        assertEquals(11500.0, summary.totalBalance, 0.001)

        // Period Income = 10000.0
        assertEquals(10000.0, summary.periodIncome, 0.001)

        // Period Expense = 2500 + 1500 = 4000.0
        assertEquals(4000.0, summary.periodExpense, 0.001)

        // Net Cash Flow = Income - Expense = 10000 - 4000 = 6000.0
        assertEquals(6000.0, summary.netCashFlow, 0.001)

        // Total Savings = 6000.0
        assertEquals(6000.0, summary.totalSavings, 0.001)

        // Top category: Groceries = 2500 / 4000 = 62.5%
        assertEquals(2, summary.topExpenseCategories.size)
        assertEquals("Groceries", summary.topExpenseCategories[0].categoryName)
        assertEquals(62.5f, summary.topExpenseCategories[0].percentage, 0.1f)
    }

    @Test
    fun testDeficitCashFlowCalculatesZeroSavings() {
        val now = System.currentTimeMillis()
        val dateRange = DateRange(now - 86400000L, now)

        val transactions = listOf(
            Transaction(
                id = 1,
                title = "Rent",
                amount = 15000.0,
                type = TransactionType.EXPENSE,
                categoryId = 6,
                accountId = 1,
                date = now
            )
        )

        val summary = calculateSummaryUseCase(
            periodTransactions = transactions,
            allAccounts = emptyList(),
            budgetsForPeriod = emptyList(),
            dateRange = dateRange
        )

        assertEquals(-15000.0, summary.netCashFlow, 0.001)
        assertEquals(0.0, summary.totalSavings, 0.001) // Savings cannot be negative
    }
}
