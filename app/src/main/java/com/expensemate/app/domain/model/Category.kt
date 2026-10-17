package com.expensemate.app.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val iconName: String,
    val colorHex: String,
    val isDefault: Boolean = false
) {
    companion object {
        val DEFAULT_EXPENSE_CATEGORIES = listOf(
            Category(1, "Food and Dining", TransactionType.EXPENSE, "restaurant", "#F97316", true),
            Category(2, "Groceries", TransactionType.EXPENSE, "shopping_cart", "#10B981", true),
            Category(3, "Transportation", TransactionType.EXPENSE, "directions_car", "#0EA5E9", true),
            Category(4, "Fuel", TransactionType.EXPENSE, "local_gas_station", "#EC4899", true),
            Category(5, "Shopping", TransactionType.EXPENSE, "shopping_bag", "#8B5CF6", true),
            Category(6, "Housing and Rent", TransactionType.EXPENSE, "home", "#6366F1", true),
            Category(7, "Electricity and Utilities", TransactionType.EXPENSE, "bolt", "#F59E0B", true),
            Category(8, "Mobile and Internet", TransactionType.EXPENSE, "wifi", "#3B82F6", true),
            Category(9, "Healthcare", TransactionType.EXPENSE, "local_hospital", "#EF4444", true),
            Category(10, "Education", TransactionType.EXPENSE, "school", "#14B8A6", true),
            Category(11, "Entertainment", TransactionType.EXPENSE, "movie", "#A855F7", true),
            Category(12, "Travel", TransactionType.EXPENSE, "flight", "#06B6D4", true),
            Category(13, "Subscriptions", TransactionType.EXPENSE, "subscriptions", "#84CC16", true),
            Category(14, "Personal Care", TransactionType.EXPENSE, "spa", "#D946EF", true),
            Category(15, "Other Expenses", TransactionType.EXPENSE, "category", "#64748B", true)
        )

        val DEFAULT_INCOME_CATEGORIES = listOf(
            Category(101, "Salary", TransactionType.INCOME, "payments", "#10B981", true),
            Category(102, "Freelancing", TransactionType.INCOME, "laptop_mac", "#3B82F6", true),
            Category(103, "Business Income", TransactionType.INCOME, "storefront", "#8B5CF6", true),
            Category(104, "Investments", TransactionType.INCOME, "trending_up", "#059669", true),
            Category(105, "Interest", TransactionType.INCOME, "account_balance", "#0284C7", true),
            Category(106, "Gifts", TransactionType.INCOME, "card_giftcard", "#EC4899", true),
            Category(107, "Other Income", TransactionType.INCOME, "savings", "#14B8A6", true)
        )
    }
}
