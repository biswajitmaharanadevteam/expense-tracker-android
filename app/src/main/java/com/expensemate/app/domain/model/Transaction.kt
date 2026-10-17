package com.expensemate.app.domain.model

enum class TransactionType {
    EXPENSE,
    INCOME
}

enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    BANK_ACCOUNT("Bank Account"),
    UPI("UPI"),
    CREDIT_CARD("Credit Card"),
    SAVINGS_ACCOUNT("Savings Account"),
    OTHER("Other")
}

data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val categoryName: String = "",
    val categoryIcon: String = "category",
    val categoryColor: String = "#2A3B90",
    val accountId: Long,
    val accountName: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val date: Long = System.currentTimeMillis(),
    val payeeOrMerchant: String = "",
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val isRecurring: Boolean = false,
    val recurringRuleId: Long? = null
)
