package com.expensemate.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.expensemate.app.domain.model.PaymentMethod
import com.expensemate.app.domain.model.Transaction
import com.expensemate.app.domain.model.TransactionType

@Entity(
    tableName = "transactions",
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
        Index(value = ["date"]),
        Index(value = ["type"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val categoryId: Long,
    val accountId: Long,
    val paymentMethod: String,
    val date: Long,
    val payeeOrMerchant: String,
    val notes: String,
    val tags: String, // Comma-separated
    val isRecurring: Boolean = false,
    val recurringRuleId: Long? = null
) {
    fun toDomain(
        categoryName: String = "",
        categoryIcon: String = "category",
        categoryColor: String = "#2A3B90",
        accountName: String = ""
    ): Transaction {
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

    companion object {
        fun fromDomain(transaction: Transaction): TransactionEntity {
            return TransactionEntity(
                id = transaction.id,
                title = transaction.title,
                amount = transaction.amount,
                type = transaction.type.name,
                categoryId = transaction.categoryId,
                accountId = transaction.accountId,
                paymentMethod = transaction.paymentMethod.name,
                date = transaction.date,
                payeeOrMerchant = transaction.payeeOrMerchant,
                notes = transaction.notes,
                tags = transaction.tags.joinToString(","),
                isRecurring = transaction.isRecurring,
                recurringRuleId = transaction.recurringRuleId
            )
        }
    }
}
