package com.expensemate.app.domain.model

enum class AccountType(val displayName: String) {
    CASH("Cash"),
    BANK("Bank Account"),
    UPI("UPI"),
    CREDIT_CARD("Credit Card"),
    SAVINGS("Savings Account"),
    CUSTOM("Custom Account")
}

data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val initialBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val colorHex: String = "#2A3B90",
    val isDefault: Boolean = false
) {
    companion object {
        val DEFAULT_ACCOUNTS = listOf(
            Account(1, "Cash", AccountType.CASH, 0.0, 0.0, "#10B981", true),
            Account(2, "Bank Account", AccountType.BANK, 0.0, 0.0, "#2A3B90", false),
            Account(3, "UPI Wallet", AccountType.UPI, 0.0, 0.0, "#0EA5E9", false),
            Account(4, "Credit Card", AccountType.CREDIT_CARD, 0.0, 0.0, "#EC4899", false),
            Account(5, "Savings Account", AccountType.SAVINGS, 0.0, 0.0, "#00897B", false)
        )
    }
}
