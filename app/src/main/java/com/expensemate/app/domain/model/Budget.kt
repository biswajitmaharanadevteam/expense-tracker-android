package com.expensemate.app.domain.model

data class Budget(
    val id: Long = 0,
    val categoryId: Long? = null, // null denotes overall monthly spending budget
    val categoryName: String = "Overall Budget",
    val categoryIcon: String = "account_balance_wallet",
    val categoryColor: String = "#2A3B90",
    val month: Int, // 1 to 12
    val year: Int,
    val amountLimit: Double,
    val amountSpent: Double = 0.0
) {
    val remainingAmount: Double
        get() = (amountLimit - amountSpent).coerceAtLeast(0.0)

    val percentageSpent: Float
        get() = if (amountLimit > 0) ((amountSpent / amountLimit) * 100).toFloat() else 0f

    val isOverBudget: Boolean
        get() = amountSpent > amountLimit

    val isNearBudget: Boolean
        get() = percentageSpent >= 80f && !isOverBudget
}
