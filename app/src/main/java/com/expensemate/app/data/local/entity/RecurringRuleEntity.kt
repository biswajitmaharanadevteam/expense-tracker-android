package com.expensemate.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.expensemate.app.domain.model.PaymentMethod
import com.expensemate.app.domain.model.RecurrenceFrequency
import com.expensemate.app.domain.model.RecurringRule
import com.expensemate.app.domain.model.TransactionType

@Entity(
    tableName = "recurring_rules",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["accountId"]),
        Index(value = ["isActive"])
    ]
)
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String,
    val categoryId: Long,
    val accountId: Long,
    val paymentMethod: String,
    val frequency: String,
    val startDate: Long,
    val endDate: Long? = null,
    val lastExecutedDate: Long? = null,
    val isActive: Boolean = true,
    val reminderEnabled: Boolean = true
) {
    fun toDomain(categoryName: String = "", accountName: String = ""): RecurringRule {
        return RecurringRule(
            id = id,
            title = title,
            amount = amount,
            type = if (type == TransactionType.INCOME.name) TransactionType.INCOME else TransactionType.EXPENSE,
            categoryId = categoryId,
            categoryName = categoryName,
            accountId = accountId,
            accountName = accountName,
            paymentMethod = try {
                PaymentMethod.valueOf(paymentMethod)
            } catch (e: Exception) {
                PaymentMethod.CASH
            },
            frequency = try {
                RecurrenceFrequency.valueOf(frequency)
            } catch (e: Exception) {
                RecurrenceFrequency.MONTHLY
            },
            startDate = startDate,
            endDate = endDate,
            lastExecutedDate = lastExecutedDate,
            isActive = isActive,
            reminderEnabled = reminderEnabled
        )
    }

    companion object {
        fun fromDomain(rule: RecurringRule): RecurringRuleEntity {
            return RecurringRuleEntity(
                id = rule.id,
                title = rule.title,
                amount = rule.amount,
                type = rule.type.name,
                categoryId = rule.categoryId,
                accountId = rule.accountId,
                paymentMethod = rule.paymentMethod.name,
                frequency = rule.frequency.name,
                startDate = rule.startDate,
                endDate = rule.endDate,
                lastExecutedDate = rule.lastExecutedDate,
                isActive = rule.isActive,
                reminderEnabled = rule.reminderEnabled
            )
        }
    }
}
