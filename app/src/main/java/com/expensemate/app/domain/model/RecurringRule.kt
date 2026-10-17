package com.expensemate.app.domain.model

enum class RecurrenceFrequency(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

data class RecurringRule(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val categoryName: String = "",
    val accountId: Long,
    val accountName: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val frequency: RecurrenceFrequency,
    val startDate: Long,
    val endDate: Long? = null,
    val lastExecutedDate: Long? = null,
    val isActive: Boolean = true,
    val reminderEnabled: Boolean = true
)
