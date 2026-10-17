package com.expensemate.app.domain.model

enum class PeriodType(val displayName: String) {
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    THIS_YEAR("This Year"),
    CUSTOM("Custom")
}

data class DateRange(
    val startMillis: Long,
    val endMillis: Long,
    val label: String = ""
)

data class CategorySpending(
    val categoryId: Long,
    val categoryName: String,
    val colorHex: String,
    val iconName: String,
    val totalAmount: Double,
    val percentage: Float
)

data class DailySpending(
    val dateMillis: Long,
    val label: String,
    val expenseAmount: Double,
    val incomeAmount: Double
)

data class MonthlyComparison(
    val monthLabel: String,
    val month: Int,
    val year: Int,
    val incomeAmount: Double,
    val expenseAmount: Double
) {
    val netSavings: Double get() = incomeAmount - expenseAmount
}

data class FinancialSummary(
    val totalBalance: Double = 0.0,
    val periodIncome: Double = 0.0,
    val periodExpense: Double = 0.0,
    val netCashFlow: Double = 0.0, // income - expense
    val remainingBudget: Double = 0.0,
    val totalSavings: Double = 0.0,
    val topExpenseCategories: List<CategorySpending> = emptyList(),
    val dailyTrend: List<DailySpending> = emptyList(),
    val largestTransactions: List<Transaction> = emptyList(),
    val averageDailySpending: Double = 0.0
)
