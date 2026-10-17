package com.expensemate.app.domain.usecase

import com.expensemate.app.domain.model.Account
import com.expensemate.app.domain.model.Budget
import com.expensemate.app.domain.model.CategorySpending
import com.expensemate.app.domain.model.DailySpending
import com.expensemate.app.domain.model.DateRange
import com.expensemate.app.domain.model.FinancialSummary
import com.expensemate.app.domain.model.Transaction
import com.expensemate.app.domain.model.TransactionType
import com.expensemate.app.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalculateFinancialSummaryUseCase {

    operator fun invoke(
        periodTransactions: List<Transaction>,
        allAccounts: List<Account>,
        budgetsForPeriod: List<Budget>,
        dateRange: DateRange
    ): FinancialSummary {
        // Total balance across all accounts
        val totalBalance = allAccounts.sumOf { it.currentBalance }

        // Period Income and Expenses
        val periodIncome = periodTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val periodExpense = periodTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        // Net cash flow = period income - period expense
        val netCashFlow = periodIncome - periodExpense

        // Remaining overall budget
        val overallBudget = budgetsForPeriod.firstOrNull { it.categoryId == null }
        val remainingBudget = if (overallBudget != null) {
            overallBudget.remainingAmount
        } else {
            val totalBudgetLimits = budgetsForPeriod.sumOf { it.amountLimit }
            val totalBudgetSpent = budgetsForPeriod.sumOf { it.amountSpent }
            (totalBudgetLimits - totalBudgetSpent).coerceAtLeast(0.0)
        }

        // Savings = net positive cash flow (or 0 if negative)
        val totalSavings = if (netCashFlow > 0) netCashFlow else 0.0

        // Top Expense Categories
        val expenseTx = periodTransactions.filter { it.type == TransactionType.EXPENSE }
        val categoryGroups = expenseTx.groupBy { it.categoryId }
        val topCategories = categoryGroups.map { (catId, txList) ->
            val first = txList.first()
            val total = txList.sumOf { it.amount }
            val pct = if (periodExpense > 0) ((total / periodExpense) * 100).toFloat() else 0f
            CategorySpending(
                categoryId = catId,
                categoryName = first.categoryName,
                colorHex = first.categoryColor,
                iconName = first.categoryIcon,
                totalAmount = total,
                percentage = pct
            )
        }.sortedByDescending { it.totalAmount }

        // Daily Spending Trend
        val dayFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val dayGroups = periodTransactions.groupBy { DateUtils.getStartOfDay(it.date) }
        val dailyTrend = dayGroups.map { (dayMillis, txList) ->
            DailySpending(
                dateMillis = dayMillis,
                label = dayFormat.format(Date(dayMillis)),
                expenseAmount = txList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
                incomeAmount = txList.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            )
        }.sortedBy { it.dateMillis }

        // Largest Transactions
        val largestTransactions = expenseTx.sortedByDescending { it.amount }.take(5)

        // Average Daily Spending
        val durationDays = ((dateRange.endMillis - dateRange.startMillis) / (24L * 60 * 60 * 1000)).coerceAtLeast(1)
        val averageDailySpending = periodExpense / durationDays

        return FinancialSummary(
            totalBalance = totalBalance,
            periodIncome = periodIncome,
            periodExpense = periodExpense,
            netCashFlow = netCashFlow,
            remainingBudget = remainingBudget,
            totalSavings = totalSavings,
            topExpenseCategories = topCategories,
            dailyTrend = dailyTrend,
            largestTransactions = largestTransactions,
            averageDailySpending = averageDailySpending
        )
    }
}
